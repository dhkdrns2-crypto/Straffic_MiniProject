package com.example.straffic.board.controller;

import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardDeleteController {
    private final BoardService boardService;

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String requesterId = auth != null ? auth.getName() : null;
        boardService.delete(id, requesterId, isAdmin);
        return "redirect:/board/list";
    }
}

