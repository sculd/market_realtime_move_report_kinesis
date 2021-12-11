import sys, os
sys.path.append(os.getcwd())
import argparse

from devops.common import run_command_print
from devops.common import CLUSTER_NAME, CLUSTER_ZONE, NAMESPACE

_PROJECT_ID = os.getenv('GOOGLE_CLOUD_PROJECT')

_DUMMY_VALUE_STRING = '---------CHANGE-ME------------'

_cluster_map = {
    _PROJECT_ID: CLUSTER_NAME
}

_zone_map = {
    _PROJECT_ID: CLUSTER_ZONE
}


def _delete_secret(secret_name):
    command = ['kubectl', 'delete', 'secret', secret_name, '-n', NAMESPACE]
    run_command_print(command)
    return True

def _check_secret(file_name):
    for blist in open(file_name, 'rb'):
        try:
            line = blist.decode('utf-8')
            if _DUMMY_VALUE_STRING in line:
                return False
        except UnicodeError:
            print('%s is not a text file, thus skipping the dummy value check.' % (file_name))
            break
    return True

def _create_secret(secret_name, file_name):
    if not _check_secret(file_name):
        return False
    command = ['kubectl', 'create', 'secret', 'generic', secret_name, '-n', NAMESPACE, '--from-file=%s' % (file_name)]
    run_command_print(command)
    return True

def _update(secret_name, file_name):
    if not _check_secret(file_name):
        print('Would not update the secret %s as it appears to be corrupt.' % (secret_name))
        return False
    if not _delete_secret(secret_name):
        return False
    return _create_secret(secret_name, file_name)

def update_kubernetes_secrets():
    command = ['gcloud', 'container', 'clusters', 'get-credentials', CLUSTER_NAME, '--zone', CLUSTER_ZONE, '--project', os.getenv('GOOGLE_CLOUD_PROJECT')]
    # this needs to be run (otherwise it fails when a cluster was deleted and re-created
    run_command_print(command)

    updates = [
        ('stream-publisher-config-json', './k8s/secrets/config.json'),
        ('stream-publisher-service-account-json', './credential.json'),
        ]

    for secret, file in updates:
        if not _update(secret, file):
            sys.exit('error occurred during updating the secret %s from file %s' % (secret, file))
    return True

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    args = parser.parse_args()
    update_kubernetes_secrets()



