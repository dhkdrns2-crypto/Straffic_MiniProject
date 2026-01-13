package com.example.straffic.board.controller;

import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.entity.CommentEntity;
import com.example.straffic.board.repository.BoardRepository;
import com.example.straffic.board.repository.CommentRepository;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class CommentController {
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    @PostMapping("/{boardId}/comment")
    public String add(@PathVariable Long boardId,
                      @RequestParam String content,
                      Authentication auth) {
        BoardEntity board = boardRepository.findById(boardId).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
        MemberEntity author = auth != null ? memberRepository.findOneById(auth.getName()) : null;
        if (author == null) throw new IllegalStateException("댓글 작성 권한이 없습니다");
        CommentEntity c = new CommentEntity();
        c.setBoard(board);
        c.setAuthor(author);
        c.setContent(content);
        commentRepository.save(c);
        return "redirect:/board/view/" + boardId;
    }

    @PostMapping("/comment/{commentId}/delete")
    public String delete(@PathVariable Long commentId, Authentication auth) {
        CommentEntity c = commentRepository.findById(commentId).orElseThrow(() -> new IllegalStateException("댓글을 찾을 수 없습니다"));
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isAuthor = auth != null && c.getAuthor() != null && c.getAuthor().getId().equals(auth.getName());
        if (!(isAdmin || isAuthor)) {
            throw new IllegalStateException("삭제 권한이 없습니다");
        }
        Long boardId = c.getBoard().getId();
        commentRepository.delete(c);
        return "redirect:/board/view/" + boardId;
    }

    @PostMapping("/comment/{commentId}/edit")
    public String edit(@PathVariable Long commentId,
                       @RequestParam String content,
                       Authentication auth) {
        CommentEntity c = commentRepository.findById(commentId).orElseThrow(() -> new IllegalStateException("댓글을 찾을 수 없습니다"));
        boolean isAuthor = auth != null && c.getAuthor() != null && c.getAuthor().getId().equals(auth.getName());
        if (!isAuthor) {
            throw new IllegalStateException("수정 권한이 없습니다");
        }
        c.setContent(content);
        commentRepository.save(c);
        return "redirect:/board/view/" + c.getBoard().getId();
    }
}

