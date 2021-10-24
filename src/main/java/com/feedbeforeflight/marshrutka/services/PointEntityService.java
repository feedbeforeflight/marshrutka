package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.PointEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PointEntityService {

    private final PointRepository pointRepository;

    public PointEntityService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public List<PointEntity> getAll() {
        return StreamSupport.stream(pointRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public PointEntity getById(int id) {
        return pointRepository.findById(id).orElse(null);
    }

    public void createPoint(PointEntity point) {
        pointRepository.save(point);
    }

    public void updatePoint(PointEntity point) {
        pointRepository.save(point);
    }

    public void deletePoint(PointEntity point) {
        pointRepository.delete(point);
    }

}
