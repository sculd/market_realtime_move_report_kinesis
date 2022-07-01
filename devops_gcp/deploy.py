import sys, os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = os.path.join(os.getcwd(), 'credential.json')
sys.path.append(os.getcwd())

import time, argparse, datetime, json, sys, docker

from devops_gcp.common import run_command, run_command_print, run_commands_until, run_command_until
from devops_gcp.common import CLUSTER_NAME, CLUSTER_ZONE, NAMESPACE

_DOCKER_VERSION_GCLOUD_DOCKER_NO_LONGER_SUPPORTED = '18.03'

_SOURCE_TRADE_BINANCE = 'trade_binance'
_CONTAINER_REGISTRY_NAME = 'market-realtime-trading-binance-kinesis'
_CONFIG_YAML_FILE_TRADE_BINANCE = 'k8s/yaml/trade_binance.yaml'
_KUBE_WORKLOAD_NAME_FORMAT = 'market-stream-publisher-{source}'

def deploy(source=_SOURCE_TRADE_BINANCE):
    if sys.version_info.major < 3:
        print ('Please use Python3 (preferably with venv)')
        sys.exit(1)

    project_id = os.getenv('GOOGLE_CLOUD_PROJECT')

    command = ['gcloud', 'container', 'clusters', 'get-credentials', CLUSTER_NAME, '--zone', CLUSTER_ZONE, '--project', project_id]
    # this needs to be run (otherwise it fails when a cluster was deleted and re-created
    run_command_print(command)

    def get_workload_name():
        return _KUBE_WORKLOAD_NAME_FORMAT.format(source=source)

    workload_name = get_workload_name()

    def get_yaml_config_name():
        if source == _SOURCE_TRADE_BINANCE:
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

if __name__ == '__main__':
    deploy()