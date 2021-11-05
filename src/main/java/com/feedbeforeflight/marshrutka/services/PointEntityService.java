package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.models.PointLiveData;
import com.feedbeforeflight.marshrutka.transport.MessageBrokerManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PointEntityService {

    private final PointRepository pointRepository;
    private final MessageBrokerManager messageBrokerManager;

    public PointEntityService(PointRepository pointRepository, MessageBrokerManager messageBrokerManager) {
        this.pointRepository = pointRepository;
        this.messageBrokerManager = messageBrokerManager;
    }

    public List<PointEntity> getAll() {
        return StreamSupport.stream(pointRepository.findAll().spliterator(), false)
                .map(m -> enrichWithLiveData(m, messageBrokerManager.getPointLiveData(m.getId())))
                .collect(Collectors.toList());
    }

    public PointEntity getById(int id) {
        return pointRepository.findById(id)
                .map(m -> enrichWithLiveData(m, messageBrokerManager.getPointLiveData(m.getId())))
                .orElse(null);
    }

    private static PointEntity enrichWithLiveData(PointEntity pointEntity, PointLiveData liveData) {
        if (liveData != null) {
            pointEntity.setMessagesSent(liveData.getMessagesSent());
            pointEntity.setMessagesReceived(liveData.getMessagesReceived());
            pointEntity.setMessagesQueued(liveData.getMessagesQueued());
            pointEntity.setPushErroneous(liveData.isPushErroneous());
        }

        return pointEntity;
    }

    public void createPoint(PointEntity point) {
        pointRepository.save(point);
        messageBrokerManager.applyConfiguration();
    }

    public void updatePoint(PointEntity point) {
        pointRepository.save(point);
        messageBrokerManager.applyConfiguration();
    }

    public void deletePoint(PointEntity point) {
        pointRepository.delete(point);
        messageBrokerManager.applyConfiguration();
    }

    public void deleteById(int id) {
        pointRepository.deleteById(id);
        messageBrokerManager.applyConfiguration();
    }

}
