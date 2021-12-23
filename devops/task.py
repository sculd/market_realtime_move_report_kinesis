import json
import boto3

def get_task_arns(ecs_client, group_name):
  tasks = ecs_client.list_tasks(cluster='market-signal')
  return [task_arn for task_arn in tasks['taskArns'] if group_name in ecs_client.describe_tasks(tasks = [task_arn], cluster='market-signal')['tasks'][0]['group']]

def start_task(group_name, container_name, task_definition, command):
  envfile = open('k8s/secrets/envvar.env')

  client = boto3.client('ecs')

  task_arsns = get_task_arns(client, group_name)

  for task_arn in task_arsns:
    print('deleting an ecs task {}'.format(task_arn))
    response = client.stop_task(
      cluster='market-signal',
      task=task_arn
    )

  response = client.run_task(
    cluster = 'market-signal',
    launchType = 'FARGATE',
    taskDefinition = task_definition,
    count = 1,
    group = group_name,
    platformVersion = 'LATEST',
    networkConfiguration = {
      'awsvpcConfiguration': {
          'subnets': [
              'subnet-0694757b',
              'subnet-70e04b1b'
          ],
          'assignPublicIp': 'ENABLED'
      }
    },
    overrides = {
      'containerOverrides': [
          {
              'name': container_name,
              'command': command,
              'environment': [{'name': line.split('=')[0], 'value': line.split('=')[1]} for line in envfile]
          },
      ]
    }
  )
  return str(response)
