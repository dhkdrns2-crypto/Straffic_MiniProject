package com.example.straffic.board.controller;

import com.example.straffic.board.dto.BoardUpdateDTO;
import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardUpdateController {
    private final BoardService boardService;

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        BoardEntity b = boardService.get(id);
        boolean isAuthor = auth != null && b.getAuthor() != null && b.getAuthor().getId().equals(auth.getName());
        if (!isAuthor) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다");
        }
        BoardUpdateDTO dto = new BoardUpdateDTO();
        dto.setTitle(b.getTitle());
        dto.setContent(b.getContent());
        dto.setPinned(b.isPinned());
        model.addAttribute("boardUpdateDTO", dto);
        model.addAttribute("boardId", b.getId());
        return "board/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute BoardUpdateDTO dto,
                       MultipartFile image,
                       Authentication auth) {
        String requesterId = auth != null ? auth.getName() : null;
        BoardEntity saved = boardService.update(id, dto, requesterId, image);
        return "redirect:/board/view/" + saved.getId();
    }
}
