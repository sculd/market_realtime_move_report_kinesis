# market_realtime_move_report_kinesis

## env vars
The env vars are attached to the container as `overrides` in `devops/task_<flavor>.py`.

### market signal
* SLACK_TOKEN
* SLACK_CHANNEL
* SLACK_CHANNEL_POLYGON_STOCK
* SLACK_CHANNEL_BINANCE
* SLACK_CHANNEL_OKCOIN
* SLACK_CHANNEL_KRAKEN
* AWS_ACCESS_KEY_ID
* AWS_SECRET_ACCESS_KEY
* GCP_PROJECT_ID
* GCP_BIGQUERY_DATASET_ID
* GCP_BIGQUERY_TABLE_ID_ORDERBOOK_LIQUIDITY_IMBALANCE
* NOTIFICATION_ENDPOINT
* NOTIFICATION_ENDPOINT_API_KEY

### trading
* BINANCE_API_KEY
* BINANCE_API_SECRET

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
### market signal
```
./devops/build.sh
```
### trading
```
./devops/build.sh
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

### deploy market trading
```
python devops/task_trading.py
```
