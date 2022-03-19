# market_realtime_move_report_kinesis

## env vars

* SLACK_TOKEN
* SLACK_CHANNEL
* NOTIFICATION_ENDPOINT
* NOTIFICATION_ENDPOINT_API_KEY


## build steps

### Tutorial
https://github.com/aws-samples/amazon-ecs-mythicalmysfits-workshop/tree/master/workshop-1

### Steps
```
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 212875327140.dkr.ecr.us-east-2.amazonaws.com       
```

```
export ECR_REPOSITORY_URI="212875327140.dkr.ecr.us-east-2.amazonaws.com/market_realtime_move_report_kinesis"       
```

```
docker build -t market-realtime-move-report-kinesis .
```

```
docker tag market-realtime-move-report-kinesis:latest ${ECR_REPOSITORY_URI}:latest
```

```
docker push ${ECR_REPOSITORY_URI}:latest
```

## build script
```
./devops/build.sh
```

## update secrete
```
python devops/manage_kube_secrets.py
```

## deploy
### deploy sudden move (bwt) market signal pusher
```
python devops/task_bwt.py
```

### deploy orderbook market signal pusher
```
python devops/task_orderbook.py
```
