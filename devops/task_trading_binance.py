import task

task.start_task('trading', 'market_trading_binance_kinesis', 'market_trading_binance_kinesis:1',
    ['java', '-jar', 'market_realtime_move_report_kinesis-1.0-SNAPSHOT.jar', '--shardid=0', '--envfile=k8s/secrets/envvar.env', '--apptype=changes_anomaly_trading_binance'])
