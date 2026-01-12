package com.example.straffic.member.controller;

import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.interfaces.MemberServiceInterface;
import com.example.straffic.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MemberReadController {

    private final MemberService memberService;

    @GetMapping("/memberOut")
    public String memberOut(Model mo, @RequestParam(required = false, defaultValue = "0", value = "page") int page) {
        Page<MemberEntity> listPage = memberService.entitypage(page);
        int totalPage = listPage.getTotalPages();
        int nowpage = listPage.getPageable().getPageNumber() + 1;
        mo.addAttribute("nowpage", nowpage);
        mo.addAttribute("list", listPage.getContent());
        mo.addAttribute("totalPage", totalPage);
        return "/member/memberOut";
    }
}
