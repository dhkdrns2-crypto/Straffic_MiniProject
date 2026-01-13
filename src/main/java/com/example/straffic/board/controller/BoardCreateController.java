package com.example.straffic.board.controller;

import com.example.straffic.board.dto.BoardCreateDTO;
import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardCreateController {
    private final BoardService boardService;

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("boardCreateDTO", new BoardCreateDTO());
        return "board/form";
    }

    @PostMapping("/write")
    public String write(@ModelAttribute BoardCreateDTO dto,
                        @org.springframework.web.bind.annotation.RequestParam(value = "image", required = false) MultipartFile image,
                        Authentication authentication) {
        String authorId = authentication != null ? authentication.getName() : null;
        BoardEntity saved = boardService.create(dto, authorId, image);
        return "redirect:/board/view/" + saved.getId();
    }
}
