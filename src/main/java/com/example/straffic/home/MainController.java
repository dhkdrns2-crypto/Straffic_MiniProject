package com.example.straffic.home;

import com.example.straffic.notice.entity.NoticeEntity;
import com.example.straffic.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NoticeService noticeService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<NoticeEntity> recentNotices = noticeService.getRecentNotices();
        model.addAttribute("recentNotices", recentNotices);
        return "main";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
