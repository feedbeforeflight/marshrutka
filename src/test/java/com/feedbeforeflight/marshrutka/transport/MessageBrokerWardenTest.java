package com.feedbeforeflight.marshrutka.transport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class MessageBrokerWardenTest {

    @MockBean MessageBrokerManager messageBrokerManager;
    @MockBean(name = "point1") BrokerPoint point1;
    @MockBean(name = "point2") BrokerPoint point2;

    @Test
    void checkClientsAlive_WithTwoAlivePoints_ShouldUpdateEach() {
        MessageBrokerWarden warden = new MessageBrokerWarden(messageBrokerManager);

        Mockito.when(messageBrokerManager.getPointList()).thenReturn(List.of(point1, point2));
        Mockito.when(point1.receiveSuspended()).thenReturn(false);
        Mockito.when(point2.receiveSuspended()).thenReturn(false);

        warden.checkClientsAlive();

        Mockito.verify(messageBrokerManager, Mockito.times(1)).getPointList();
        Mockito.verify(point1, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point2, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point1, Mockito.times(1)).updateQueuedCount();
        Mockito.verify(point2, Mockito.times(1)).updateQueuedCount();
    }

    @Test
    void checkClientsAlive_WithOneSuspendedPointWithTimeout_ShouldResumeSuspended() {
        MessageBrokerWarden warden = new MessageBrokerWarden(messageBrokerManager);

        Mockito.when(messageBrokerManager.getPointList()).thenReturn(List.of(point1, point2));
        Mockito.when(point1.receiveSuspended()).thenReturn(false);
        Mockito.when(point2.receiveSuspended()).thenReturn(true);
        Mockito.when(point2.getLastSendToClientAttempt()).thenReturn(System.currentTimeMillis() - 12000);

        warden.checkClientsAlive();

        Mockito.verify(messageBrokerManager, Mockito.times(1)).getPointList();
        Mockito.verify(point1, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point2, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point1, Mockito.times(1)).updateQueuedCount();
        Mockito.verify(point2, Mockito.times(1)).updateQueuedCount();
        Mockito.verify(point2, Mockito.times(1)).resumeMessageReceiver();
    }

    @Test
    void checkClientsAlive_WithOneSuspendedPointWithoutTimeout_ShouldIgnoreSuspended() {
        MessageBrokerWarden warden = new MessageBrokerWarden(messageBrokerManager);

        Mockito.when(messageBrokerManager.getPointList()).thenReturn(List.of(point1, point2));
        Mockito.when(point1.receiveSuspended()).thenReturn(false);
        Mockito.when(point2.receiveSuspended()).thenReturn(true);
        Mockito.when(point2.getLastSendToClientAttempt()).thenReturn(System.currentTimeMillis());

        warden.checkClientsAlive();

        Mockito.verify(messageBrokerManager, Mockito.times(1)).getPointList();
        Mockito.verify(point1, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point2, Mockito.times(1)).receiveSuspended();
        Mockito.verify(point1, Mockito.times(1)).updateQueuedCount();
        Mockito.verify(point2, Mockito.times(1)).updateQueuedCount();
        Mockito.verify(point2, Mockito.times(0)).resumeMessageReceiver();
    }

}