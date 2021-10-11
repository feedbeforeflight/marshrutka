package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.Point;
import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.services.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/management")
public class ManagementController {

    private final PointService pointService;

    @Autowired
    public ManagementController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping()
    public String getManagementIndex() {
        return "management/index";
    }

    @GetMapping("/points")
    public String getAllPoints(Model model) {
        model.addAttribute("points", pointService.getAll());
        return "management/points";
    }

    @GetMapping("/points/{id}")
    public String showPoint(@PathVariable("id") int id, Model model) {
        Point point = pointService.getById(id);
        if (point == null) {
            return "redirect:/management/points";
        }

        model.addAttribute("point", point);
        return "management/point";
    }

    @GetMapping("/points/new")
    public String newPoint(Model model) {
        Point point = new Point();
        model.addAttribute("point", point);
        return "management/point";
    }

    @PostMapping("/points/{id}")
    public String updatePoint(@ModelAttribute("point") Point point) {
        pointService.updatePoint(point);

        return "redirect:/management/points";
    }
}
