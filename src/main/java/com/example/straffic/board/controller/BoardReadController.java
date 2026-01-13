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

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardReadController {
    private final BoardService boardService;

    @GetMapping("/list")
    public String list(@RequestParam(value = "page", defaultValue = "0") Integer page,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "type", required = false) String type,
                       Model model) {
        BoardSearchDTO search = new BoardSearchDTO();
        search.setPage(page);
        search.setQ(q);
        search.setType(type);
        Page<BoardEntity> boards = boardService.list(search);
        List<BoardEntity> top3 = boardService.top3ByViews();
        List<BoardEntity> pinned = boardService.pinnedBoards();
        model.addAttribute("boards", boards);
        model.addAttribute("top3", top3);
        model.addAttribute("pinned", pinned);
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        return "board/list";
    }
}

