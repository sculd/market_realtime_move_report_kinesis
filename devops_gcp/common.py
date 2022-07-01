import subprocess
import time

CLUSTER_NAME = 'market'
CLUSTER_ZONE = 'us-central1-c'
NAMESPACE = 'market'

def run_command(command, shell=False):
    print(' '.join(command))
    result = subprocess.run(command,
                            stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE,
                            shell=shell)
    res = []
    if result.stdout:
        res += result.stdout.decode('utf-8').split('\\n')
    if result.stderr:
        res += result.stderr.decode('utf-8').split('\\n')
    return res


def run_command_print(command, shell=False):
    lines = run_command(command, shell=shell)
    print('\n'.join(lines))


def run_commands_until(commands, target, shell=False):
    while True:
        gone = False
        lines = []
        for c in commands:
            lines += run_command(c, shell=shell)
        print('\n'.join(lines))
        for line in lines:
            if target in line:
                gone = True
                break
        if gone:
            break
        time.sleep(1)


def run_command_until(command, target, shell=False):
    run_commands_until([command], target, shell=shell)
