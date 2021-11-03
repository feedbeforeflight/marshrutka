package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.services.PointEntityService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagementController.class)
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointEntityService pointService;

    @Test
    public void contextLoads() {
        assertThat(pointService, notNullValue());
    }

    @Test
    @SneakyThrows
    void getManagementIndex() {
        mockMvc.perform(get("/management"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().string(containsString("<title>Marshrutka project / Management</title>")));
    }

    @Test
    @SneakyThrows
    void getAllPoints() {
        PointEntity point1 = new PointEntity(1, "point1", true);
        PointEntity point2 = new PointEntity(3, "point2", false);
        PointEntity point3 = new PointEntity(51, "point120", true);
        Mockito.when(pointService.getAll()).thenReturn(List.of(point1, point2, point3));

        mockMvc.perform(get("/management/points"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().string(containsString("<title>Marshrutka project / Management / Points</title>")),
                        content().string(containsString("point1")),
                        content().string(containsString("point2")),
                        content().string(containsString("point120")));
    }

    @Test
    @SneakyThrows
    void showPoint() {
        Mockito.when(pointService.getById(120)).thenReturn(new PointEntity(120, "mockedPoint", true));

        mockMvc.perform(get("/management/points/120"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().string(containsString("<title>Marshrutka project / Management / Points / Point</title>")),
                        content().string(containsString("mockedPoint")));
    }

    @Test
    @SneakyThrows
    void newPoint() {
        mockMvc.perform(get("/management/points/new"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().string(containsString("<title>Marshrutka project / Management / Points / Point</title>")));
    }

    @Test
    @SneakyThrows
    void updatePoint() {
        mockMvc.perform(post("/management/points/24")
                        .queryParam("id", "24")
                        .param("name", "updatedPoint")
                        .param("active", "true")
                )
                .andDo(print())
                .andExpectAll(
                        status().isFound(),
                        redirectedUrl("/management/points"));

        ArgumentCaptor<PointEntity> pointArgumentCaptor = ArgumentCaptor.forClass(PointEntity.class);

        Mockito.verify(pointService, Mockito.times(1)).updatePoint(pointArgumentCaptor.capture());
        assertThat(pointArgumentCaptor.getAllValues(), hasSize(1));
        PointEntity testPoint = pointArgumentCaptor.getValue();
        assertThat(testPoint.getId(), is(24));
        assertThat(testPoint.getName(), is("updatedPoint"));
        assertThat(testPoint.isActive(), is(true));
    }
}