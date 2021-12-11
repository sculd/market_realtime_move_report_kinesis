import subprocess

def run_command(command, shell=False):
    print(' '.join(command))
    result = subprocess.run(command, stdout=subprocess.PIPE, shell=shell)
    std = str(result.stdout.decode('utf8')).split('\n') if result.stdout is not None else []
    err = str(result.stderr.decode('utf8')).split('\n') if result.stderr is not None else []
    return std, err
