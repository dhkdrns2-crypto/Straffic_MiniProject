package com.example.straffic.mobility.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BikeController {
    @Value("${kakao.js.key:}")
    private String kakaoJsKey;

    @GetMapping("/bike")
    public String bikeMain(Model model) {
        model.addAttribute("pageTitle", "공유 모빌리티");
        model.addAttribute("kakaoKey", kakaoJsKey);
        return "mobility/bike";
    }
}
