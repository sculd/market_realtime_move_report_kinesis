import json
import boto3

def get_task_arns(ecs_client, group_name = 'market_realtime_move_report_kinesis'):
  tasks = ecs_client.list_tasks(cluster='market-signal')
  return [task_arn for task_arn in tasks['taskArns'] if group_name in ecs_client.describe_tasks(tasks = [task_arn], cluster='market-signal')['tasks'][0]['group']]

def start_task():
  envvars = json.load(open('k8s/secrets/envvar.json'))

  client = boto3.client('ecs')

  task_arsns = get_task_arns(client)

  for task_arn in task_arsns:
    print('deleting an ecs task {}'.format(task_arn))
    response = client.stop_task(
      cluster='market-signal',
      task=task_arn
    )

  response = client.run_task(
    cluster = 'market-signal',
    launchType = 'FARGATE',
    taskDefinition = 'market_realtime_move_report_kinesis:2',
    count = 1,
    group = 'market_realtime_move_report_kinesis',
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
              'name': 'market_realtime_move_report_kinesis',
              'environment': [{'name': k, 'value': v} for k, v in envvars.items()]
          },
      ]
    }
  )
  return str(response)

start_task()
# java -jar build/libs/sonar-alert-agent-1.0-SNAPSHOT-all.jar
# java -jar build/libs/market_realtime_move_report_kinesis-1.0-SNAPSHOT.jar --shardid=0 --envvars=k8s/secrets/envvar.json
