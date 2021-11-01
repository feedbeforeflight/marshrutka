package com.feedbeforeflight.marshrutka.transport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
public class MessageBrokerWarden {

    private int cyclesCount = 0;
    private final long pointResumeAfterErrorTimeout = 10000;

    MessageBrokerManager messageBrokerManager;

    public MessageBrokerWarden(MessageBrokerManager messageBrokerManager) {
        this.messageBrokerManager = messageBrokerManager;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkClientsAlive() {
        cyclesCount++;

//        System.out.println("Warden is awake. Cycle #" + cyclesCount);

        for (BrokerPoint brokerPoint : messageBrokerManager.getPointList()) {
            if (brokerPoint.receiveSuspended()) {
                long currentTime = System.currentTimeMillis();
                log.info("Checked at " + String.valueOf(currentTime));

                if ((currentTime - brokerPoint.getLastSendToClientAttempt()) >= pointResumeAfterErrorTimeout) {
                    log.info(String.valueOf(System.currentTimeMillis() - brokerPoint.getLastSendToClientAttempt()));
                    brokerPoint.resumeMessageReceiver();
                }
            }
        }
    }
}
