package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.Point;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
class PointServiceTest {

    @MockBean
    private PointRepository pointRepository;

    @Autowired
    private PointService pointService;

    @Test
    public void contextLoads() {
        assertThat(pointService, notNullValue());
    }

    @Test
    void getAll() {
        Point point1 = new Point(1, "point1", true);
        Point point2 = new Point(3, "point2", false);
        Point point3 = new Point(51, "point120", true);
        Mockito.when(pointRepository.findAll()).thenReturn(List.of(point1, point2, point3));

        List<Point> points = pointService.getAll();

        Mockito.verify(pointRepository, Mockito.times(1)).findAll();
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
        Point originalPoint = new Point();
        Mockito.when(pointRepository.findById(1)).thenReturn(Optional.of(originalPoint));

        Point testPoint = pointService.getById(1);

        Mockito.verify(pointRepository, Mockito.times(1)).findById(1);
        assertThat(testPoint, CoreMatchers.sameInstance(originalPoint));
    }

    @Test
    void createPoint() {
        Point originalPoint = new Point();

        pointService.createPoint(originalPoint);

        ArgumentCaptor<Point> pointArgumentCaptor = ArgumentCaptor.forClass(Point.class);

        Mockito.verify(pointRepository, Mockito.times(1)).save(pointArgumentCaptor.capture());
        assertThat(pointArgumentCaptor.getAllValues(), hasSize(1));
        Point testPoint = pointArgumentCaptor.getValue();
        assertThat(testPoint, sameInstance(originalPoint));
    }

    @Test
    void updatePoint() {
        Point point = new Point();
        pointService.updatePoint(point);

        Mockito.verify(pointRepository, Mockito.times(1)).save(point);
    }

    @Test
    void deletePoint() {
        Point point = new Point();
        pointService.deletePoint(point);

        Mockito.verify(pointRepository, Mockito.times(1)).delete(point);
    }
}