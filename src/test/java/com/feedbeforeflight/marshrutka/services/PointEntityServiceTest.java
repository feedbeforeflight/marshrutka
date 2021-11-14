package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.PointEntity;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class PointEntityServiceTest {

    @MockBean
    private PointRepository pointRepository;

    @Autowired
    private PointEntityService pointService;

    @Test
    public void contextLoads() {
        assertThat(pointService, notNullValue());
    }

    @Test
    void getAll() {
        PointEntity point1 = new PointEntity(1, "point1", true);
        PointEntity point2 = new PointEntity(3, "point2", false);
        PointEntity point3 = new PointEntity(51, "point120", true);
        Mockito.when(pointRepository.findAll()).thenReturn(List.of(point1, point2, point3));

        List<PointEntity> points = pointService.getAll();

        Mockito.verify(pointRepository, Mockito.times(2)).findAll();
        assertThat(points, hasSize(3));
        assertThat(points.get(0), notNullValue());
        assertThat(points.get(0), sameInstance(point1));
        assertThat(points.get(1), notNullValue());
        assertThat(points.get(1), sameInstance(point2));
        assertThat(points.get(2), notNullValue());
        assertThat(points.get(2), sameInstance(point3));
    }

    @Test
    void getById() {
        PointEntity originalPoint = new PointEntity();
        Mockito.when(pointRepository.findById(1)).thenReturn(Optional.of(originalPoint));

        PointEntity testPoint = pointService.getById(1);

        Mockito.verify(pointRepository, Mockito.times(1)).findById(1);
        assertThat(testPoint, CoreMatchers.sameInstance(originalPoint));
    }

    @Test
    void createPoint() {
        PointEntity originalPoint = new PointEntity();

        pointService.createPoint(originalPoint);

        ArgumentCaptor<PointEntity> pointArgumentCaptor = ArgumentCaptor.forClass(PointEntity.class);

        Mockito.verify(pointRepository, Mockito.times(1)).save(pointArgumentCaptor.capture());
        assertThat(pointArgumentCaptor.getAllValues(), hasSize(1));
        PointEntity testPoint = pointArgumentCaptor.getValue();
        assertThat(testPoint, sameInstance(originalPoint));
    }

    @Test
    void updatePoint() {
        PointEntity point = new PointEntity();
        pointService.updatePoint(point);

        Mockito.verify(pointRepository, Mockito.times(1)).save(point);
    }

    @Test
    void deletePoint() {
        PointEntity point = new PointEntity();
        pointService.deletePoint(point);

        Mockito.verify(pointRepository, Mockito.times(1)).delete(point);
    }
}