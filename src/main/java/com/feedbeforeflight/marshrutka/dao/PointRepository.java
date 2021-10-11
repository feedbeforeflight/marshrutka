package com.feedbeforeflight.marshrutka.dao;

import com.feedbeforeflight.marshrutka.models.Point;
import org.springframework.data.repository.CrudRepository;

public interface PointRepository extends CrudRepository<Point, Integer> {
}
