package com.tradingbinancechangesanomalyreversal.recordprocessor;

import com.tradingbinancechangesanomalyreversal.stream.ChangesAnomalyReversalTradingBinanceStream;

import com.marketsignal.stream.BarWithTimeStream;
import com.marketsignal.timeseries.BarWithTime;
import com.marketsignal.timeseries.BarWithTimeSlidingWindow;
import com.tradingchangesanomaly.util.ChangesAnomalyTradingInitParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.exceptions.ThrottlingException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.RecordProcessorCheckpointer;
import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.retrieval.KinesisClientRecord;

import java.time.Duration;

/**
 * The implementation of the ShardRecordProcessor interface is where the heart of the record processing logic lives.
 * In this example all we do to 'process' is log info about the records.
 */
public class BarWithTimestampRecordProcessor implements ShardRecordProcessor {

    private static final String SHARD_ID_MDC_KEY = "ShardId";

    private static final Logger log = LoggerFactory.getLogger(BarWithTimestampRecordProcessor.class);

    private String shardId;

    // Checkpointing interval
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L; // 1 minute
    private long nextCheckpointTimeInMillis;

    private long messageCount = 0;

    BarWithTimeStream barWithTimeStream = new BarWithTimeStream(Duration.ofHours(6), BarWithTimeSlidingWindow.TimeSeriesResolution.MINUTE);
    ChangesAnomalyReversalTradingBinanceStream changesAnomalyReversalTradingBinanceStream = new ChangesAnomalyReversalTradingBinanceStream(barWithTimeStream);

    /**
     * Invoked by the KCL before data records are delivered to the ShardRecordProcessor instance (via
     * processRecords). In this example we do nothing except some logging.
     *
     * @param initializationInput Provides information related to initialization.
     */
    public void initialize(InitializationInput initializationInput) {
        shardId = initializationInput.shardId();
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Initializing @ Sequence: {}", initializationInput.extendedSequenceNumber());
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;

            log.info("[initialize]");
            String tradingParamJson = System.getenv("TRADING_PARAM_JSON");
            ChangesAnomalyTradingInitParameter changesAnomalyTradingInitParameter = ChangesAnomalyTradingInitParameter.builder().build();
            changesAnomalyTradingInitParameter.initFromJson(tradingParamJson);
            log.info("changesAnomalyTradingInitParameter: {}", changesAnomalyTradingInitParameter);
            changesAnomalyReversalTradingBinanceStream.init(changesAnomalyTradingInitParameter.changesAnomalyTradingStreamInitParameter);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    /**
     * Handles record processing logic. The Amazon Kinesis Client Library will invoke this method to deliver
     * data records to the application. In this example we simply log our records.
     *
     * @param processRecordsInput Provides the records to be processed as well as information and capabilities
     *                            related to them (e.g. checkpointing).
     */
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            // Checkpoint once every checkpoint interval
            if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
                checkpoint(processRecordsInput.checkpointer());
                nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
            }

            log.info("Processing {} record(s)", processRecordsInput.records().size());
            processRecordsInput.records().forEach(this::processRecord);
        } catch (Throwable t) {
            log.error("Caught throwable while processing records. Aborting.");
            Runtime.getRuntime().halt(1);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    void processRecord(KinesisClientRecord record) {
        byte[] arr = new byte[record.data().remaining()];
        record.data().get(arr);
        BarWithTime bwt = BarWithTime.fromBytes(arr);
        messageCount += 1;
        if (messageCount % 100 == 0) {
            log.info("On 100ths message, processing bwt: {}", bwt.toString());
        }
        barWithTimeStream.onBarWithTime(bwt);
        changesAnomalyReversalTradingBinanceStream.onBarWithTime(bwt);
    }

    private void checkpoint(RecordProcessorCheckpointer checkpointer) {
        log.info("Checkpointing shard " + shardId);
        try {
            checkpointer.checkpoint();
        } catch (ShutdownException se) {
            // Ignore checkpoint if the processor instance has been shutdown (fail over).
            log.info("Caught shutdown exception, skipping checkpoint.", se);
        } catch (ThrottlingException e) {
            // Skip checkpoint when throttled. In practice, consider a backoff and retry policy.
            log.error("Caught throttling exception, skipping checkpoint.", e);
        } catch (InvalidStateException e) {
            // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
            log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
        }
    }

    /** Called when the lease tied to this record processor has been lost. Once the lease has been lost,
     * the record processor can no longer checkpoint.
     *
     * @param leaseLostInput Provides access to functions and data related to the loss of the lease.
     */
    public void leaseLost(LeaseLostInput leaseLostInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Lost lease, so terminating.");
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    /**
     * Called when all data on this shard has been processed. Checkpointing must occur in the method for record
     * processing to be considered complete; an exception will be thrown otherwise.
     *
     * @param shardEndedInput Provides access to a checkpointer method for completing processing of the shard.
     */
    public void shardEnded(ShardEndedInput shardEndedInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Reached shard end checkpointing.");
            shardEndedInput.checkpointer().checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.error("Exception while checkpointing at shard end. Giving up.", e);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    /**
     * Invoked when Scheduler has been requested to shut down (i.e. we decide to stop running the app by pressing
     * Enter). Checkpoints and logs the data a final time.
     *
     * @param shutdownRequestedInput Provides access to a checkpointer, allowing a record processor to checkpoint
     *                               before the shutdown is completed.
     */
    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Scheduler is shutting down, checkpointing.");
            shutdownRequestedInput.checkpointer().checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.error("Exception while checkpointing at requested shutdown. Giving up.", e);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }
}