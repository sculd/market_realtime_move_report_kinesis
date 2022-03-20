import task

task.start_task('market_orderbook_report_kinesis', 'market_realtime_move_report_kinesis', 'market_realtime_move_report_kinesis:2',
    ['java', '-jar', 'market_realtime_move_report_kinesis-1.0-SNAPSHOT.jar', '--shardid=0', '--envfile=k8s/secrets/envvar.env', '--apptype=orderbook_anomaly_stream'])
