package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.Point;
import com.feedbeforeflight.marshrutka.models.PointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/management")
public class ManagementController {

    private final PointRepository pointRepository;

    @Autowired
    public ManagementController(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @GetMapping()
    public String getManagementIndex() {
        return "management/index";
    }

    @GetMapping("/points")
    public String getAllPoints(Model model) {
        model.addAttribute("points", pointRepository.findAll());
        return "management/points";
    }

    @GetMapping("/points/{id}")
    public String showPoint(@PathVariable("id") int id, Model model) {
        Point point = pointRepository.findById(id).orElse(null);
        if (point == null) {
            return "redirect:/management/points";
        }

        model.addAttribute("point", point);
        return "management/point";
    }
}
