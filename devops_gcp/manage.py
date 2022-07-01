import sys, os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.path.join(os.getcwd(), 'credential.json')
sys.path.append(os.getcwd())

import time, argparse, datetime, json, sys, docker

from devops_gcp.common import run_command, run_command_print, run_commands_until, run_command_until
from devops_gcp.update_secrets import update_kubernetes_secrets
from devops_gcp.common import CLUSTER_NAME, CLUSTER_ZONE, NAMESPACE

_DOCKER_VERSION_GCLOUD_DOCKER_NO_LONGER_SUPPORTED = '18.03'

_SOURCE_TRADE_BINANCE = 'trade_binance'
_CONTAINER_REGISTRY_NAME = 'market-realtime-trading-binance-kinesis'
_CONFIG_YAML_FILE_TRADE_BINANCE = 'k8s/yamls/trade_binance.yaml'
_KUBE_WORKLOAD_NAME_FORMAT = 'market-stream-publisher-{source}'

def run_docker_push(docker_image_tag):
    print('[run_docker_push]')
    docker_client = docker.from_env()
    version_str = docker_client.version()['Version']
    print('docker version: ', version_str)
    if version_str < _DOCKER_VERSION_GCLOUD_DOCKER_NO_LONGER_SUPPORTED:
        run_command_print(['gcloud', 'docker', '--', 'push', docker_image_tag])
    else:
        print('Make sure "gcloud auth configure-docker" was run once, as your docker version is >= 18.03.')
        run_command_print(['docker', '--', 'push', docker_image_tag])

if __name__ == '__main__':
    if sys.version_info.major < 3:
        print ('Please use Python3 (preferably with venv)')
        sys.exit(1)

    parser = argparse.ArgumentParser()
    parser.add_argument("action", type=str, choices=["build", "secret-update", "deploy", "delete"])
    parser.add_argument("-s", "--source", help="{s} (only for deploy or delete action)".format(s=_SOURCE_TRADE_BINANCE), type=str, choices=[_SOURCE_TRADE_BINANCE])
    parser.add_argument("-n", "--namespace", type=str, help="{n} kubernetes namespace".format(n='market'), default="market")
    args = parser.parse_args()

    project_id = os.getenv('GOOGLE_CLOUD_PROJECT')

    # confirm before applying action
    prompt_string = "applying [{action}]. Continue?(y/n) " \
        .format(action=args.action)
    if args.action == "secret-update":
        prompt_string = "applying [secret-update]. Continue?(y/n)"

    response = input(prompt_string)
    if response != 'y':
        sys.exit(0)

    command = ['gcloud', 'container', 'clusters', 'get-credentials', CLUSTER_NAME, '--zone', CLUSTER_ZONE, '--project', project_id]
    # this needs to be run (otherwise it fails when a cluster was deleted and re-created
    run_command_print(command)

    def get_workload_name():
        return _KUBE_WORKLOAD_NAME_FORMAT.format(source=args.source)

    if args.action == "build":
        docker_image_tag = 'gcr.io/{p}/{c}'.format(p=project_id, c=_CONTAINER_REGISTRY_NAME)
        run_command_print(['docker', 'build', '.', '-f', 'devops/Dockerfile', '-t', docker_image_tag, '--platform', 'linux/x86_64'])

        run_docker_push(docker_image_tag)

    elif args.action == "secret-update":
        update_kubernetes_secrets()

    elif args.action in ("deploy"):
        workload_name = get_workload_name()

        def get_yaml_config_name():
            if args.source == _SOURCE_TRADE_BINANCE:
                return _CONFIG_YAML_FILE_TRADE_BINANCE

        run_command_print(['kubectl', 'delete', 'job', workload_name, '-n', NAMESPACE])
        run_command_print(['kubectl', 'delete', 'deployment', workload_name, '-n', NAMESPACE])
        run_command_until(['kubectl', 'get', 'pod', workload_name, '-n', NAMESPACE], "not found")
        run_command_print(['kubectl', 'create', '-n', NAMESPACE, '-f', get_yaml_config_name()])
        time.sleep(3)

        run_commands_until([
            ['kubectl', '-n', NAMESPACE, 'get', 'deployments'],
            ['kubectl', '-n', NAMESPACE, 'get', 'jobs'],
            ['kubectl', '-n', NAMESPACE, 'get', 'pods']
        ], workload_name)

    elif args.action == "delete":
        run_command_print(['kubectl', 'delete', 'deployment', get_workload_name()])
