package com.example.straffic.mobility.controller;

import com.example.straffic.dashboard.service.PageViewStatsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BikeController {

    private final PageViewStatsService pageViewStatsService;

    public BikeController(PageViewStatsService pageViewStatsService) {
        this.pageViewStatsService = pageViewStatsService;
    }

    @Value("${kakao.js.key:}")
    private String kakaoJsKey;

    @GetMapping("/wayfinding")
    public String bikeMain(Model model) {
        pageViewStatsService.increaseView("WAYFINDING");
        model.addAttribute("pageTitle", "길찾기");
        model.addAttribute("kakaoKey", kakaoJsKey);
        return "mobility/route";
    }
}
