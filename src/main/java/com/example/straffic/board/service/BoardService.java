package com.example.straffic.board.service;

import com.example.straffic.board.dto.BoardCreateDTO;
import com.example.straffic.board.dto.BoardResponseDTO;
import com.example.straffic.board.dto.BoardSearchDTO;
import com.example.straffic.board.dto.BoardUpdateDTO;
import com.example.straffic.board.entity.BoardEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    BoardEntity create(BoardCreateDTO dto, String authorId, MultipartFile image);
    BoardEntity update(Long id, BoardUpdateDTO dto, String requesterId, MultipartFile image);
    void delete(Long id, String requesterId, boolean requesterIsAdmin);
    Page<BoardEntity> list(BoardSearchDTO search);
    BoardEntity getAndIncreaseViews(Long id);
    List<BoardEntity> top3ByViews();
    byte[] getImageData(Long boardId);
    List<BoardEntity> pinnedBoards();
    BoardEntity get(Long id);
}
