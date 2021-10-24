package com.feedbeforeflight.marshrutka.dao;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import org.springframework.data.repository.CrudRepository;

public interface PointRepository extends CrudRepository<PointEntity, Integer> {
}
