package com.marketsignal.publish;

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

    public void publish(ChangesAnomaly.Anomaly anomaly) {
        String token = System.getenv("SLACK_TOKEN");
        String channel = System.getenv("SLACK_CHANNEL");

        log.info("[SlackPublisher] publishing a new anomaly: {}", anomaly.toString());
        try {
            ChatPostMessageResponse response = slack.methods(token).chatPostMessage(req -> req
                    .channel(channel) // Channel ID
                    .text(anomaly.toString()));
        } catch (IOException ex) {
            log.error(ex.getMessage());
        } catch (SlackApiException ex) {
            log.error(ex.getMessage());
        }
    }
}
