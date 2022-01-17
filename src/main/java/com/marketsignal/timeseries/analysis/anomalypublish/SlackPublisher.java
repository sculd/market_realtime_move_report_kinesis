package com.marketsignal.timeseries.analysis.anomalypublish;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;

import com.marketsignal.timeseries.analysis.ChangesAnomaly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SlackPublisher {
    private static final Logger log = LoggerFactory.getLogger(SlackPublisher.class);
    Slack slack;

    public SlackPublisher() {
        slack = Slack.getInstance();
    }

    public boolean isNonTrivialAnomaly(ChangesAnomaly.Anomaly anomaly) {
        return anomaly.changeThreshold > 0.05;
    }

    private void publishToChannel(ChangesAnomaly.Anomaly anomaly, final String channel) {
        String token = System.getenv("SLACK_TOKEN");

        log.info("[SlackPublisher] publishing a new anomaly: {}", anomaly.toString());
        try {
            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                    .channel(channel) // Channel ID
                    .text(anomaly.toString()));
            if (!response.isOk()) {
                log.error(response.getMessage().toString());
            }
        } catch (IOException ex) {
            log.error(ex.getMessage());
        } catch (SlackApiException ex) {
            log.error(ex.getMessage());
        }
    }

    public void publish(ChangesAnomaly.Anomaly anomaly) {
        String channel = System.getenv("SLACK_CHANNEL");
        String channel_polygon_stock = System.getenv("SLACK_CHANNEL_POLYGON_STOCK");
        String channel_binance = System.getenv("SLACK_CHANNEL_BINANCE");
        String channel_okcoin = System.getenv("SLACK_CHANNEL_OKCOIN");
        String channel_kraken = System.getenv("SLACK_CHANNEL_KRAKEN");

        publishToChannel(anomaly, channel);
        if (anomaly.market.equals("polygon")) {
            log.info("publishing to the polygon slack channel");
            publishToChannel(anomaly, channel_polygon_stock);
        } else if (anomaly.market.equals("binance")) {
            log.info("publishing to the binance slack channel");
            publishToChannel(anomaly, channel_binance);
        } else if (anomaly.market.equals("okcoin")) {
            log.info("publishing to the okcoin slack channel");
            publishToChannel(anomaly, channel_okcoin);
        } else if (anomaly.market.equals("kraken")) {
            log.info("publishing to the kraken slack channel");
            publishToChannel(anomaly, channel_kraken);
        }
    }
}
