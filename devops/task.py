import json
import boto3

def start_task():
  envvars = json.load(open('k8s/secrets/envvar.json'))

  client = boto3.client('ecs')

  tasks = client.list_tasks(
    cluster='market-signal',
    launchType='FARGATE'
  )

  for task_arn in tasks['taskArns']:
    response = client.stop_task(
      cluster='market-signal',
      task=task_arn
    )

  response = client.run_task(
    cluster = 'market-signal',
    launchType = 'FARGATE',
    taskDefinition = 'market_realtime_move_report_kinesis:2',
    count = 1,
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
