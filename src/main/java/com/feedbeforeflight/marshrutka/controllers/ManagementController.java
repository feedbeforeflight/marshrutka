package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.services.PointEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/management")
public class ManagementController {

    private final PointEntityService pointService;

    @Autowired
    public ManagementController(PointEntityService pointService) {
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
        PointEntity point = pointService.getById(id);
        if (point == null) {
            return "redirect:/management/points";
        }

        model.addAttribute("point", point);
        return "management/point";
    }

    @GetMapping("/points/{id}/edit")
    public String editPoint(@PathVariable("id") int id, Model model) {
        PointEntity point = pointService.getById(id);
        if (point == null) {
            return "redirect:/management/points";
        }

        model.addAttribute("point", point);
        return "management/edit_point";
    }

    @GetMapping("/points/new")
    public String newPoint(Model model) {
        PointEntity point = new PointEntity();
        model.addAttribute("point", point);
        return "management/edit_point";
    }

    @PostMapping("/points/{id}")
    public String updatePoint(@ModelAttribute("point") PointEntity point) {
        pointService.updatePoint(point);

        return "redirect:/management/points";
    }
}
