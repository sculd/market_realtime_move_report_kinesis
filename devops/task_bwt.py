import task

task.start_task('market_realtime_move_report_kinesis', 'market_realtime_move_report_kinesis', 'market_realtime_move_report_kinesis:2',
    ['java', '-jar', 'market_realtime_move_report_kinesis-1.0-SNAPSHOT.jar', '--shardid=0', '--envvars=k8s/secrets/envvars.env', '--apptype=changes_anomaly_stream'])
