package com.example.straffic.board.controller;

import com.example.straffic.board.dto.BoardSearchDTO;
import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardSearchController {
    private final BoardService boardService;

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", required = false) String q,
                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                         Model model) {
        BoardSearchDTO search = new BoardSearchDTO();
        search.setQ(q);
        search.setPage(page);
        Page<BoardEntity> boards = boardService.list(search);
        model.addAttribute("pinned", boardService.pinnedBoards());
        model.addAttribute("top3", boardService.top3ByViews());
        model.addAttribute("boards", boards);
        model.addAttribute("q", q);
        return "board/list";
    }
}
