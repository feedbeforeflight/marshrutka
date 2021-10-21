package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    public List<Point> getAll() {
        return StreamSupport.stream(pointRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }

    public Point getById(int id) {
        return pointRepository.findById(id).orElse(null);
    }

    public void createPoint(Point point) {
        pointRepository.save(point);
    }

    public void updatePoint(Point point) {
        pointRepository.save(point);
    }

    public void deletePoint(Point point) {
        pointRepository.delete(point);
    }

}
