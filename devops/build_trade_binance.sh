gradle jar
aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin 212875327140.dkr.ecr.us-east-2.amazonaws.com
export ECR_REPOSITORY_URI="212875327140.dkr.ecr.us-east-2.amazonaws.com/market-realtime-trading-binance-kinesis"
docker build -t market-realtime-trading-binance-kinesis . -f Dockerfile --platform linux/x86_64
docker tag market-realtime-trading-binance-kinesis:latest ${ECR_REPOSITORY_URI}:latest
docker push ${ECR_REPOSITORY_URI}:latest