package com.example.straffic.board.serviceImp;

import com.example.straffic.board.dto.BoardCreateDTO;
import com.example.straffic.board.dto.BoardResponseDTO;
import com.example.straffic.board.dto.BoardSearchDTO;
import com.example.straffic.board.dto.BoardUpdateDTO;
import com.example.straffic.board.entity.BoardEntity;
import com.example.straffic.board.repository.BoardRepository;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public BoardEntity create(BoardCreateDTO dto, String authorId, MultipartFile image) {
        MemberEntity author = memberRepository.findOneById(authorId);
        if (author == null) throw new IllegalStateException("작성자를 찾을 수 없습니다");
        BoardEntity b = new BoardEntity();
        b.setTitle(dto.getTitle());
        b.setContent(dto.getContent());
        b.setAuthor(author);

        boolean isAdmin = author.getRole() != null && author.getRole().contains("ADMIN");
        b.setPinned(isAdmin && dto.isPinned());

        if (image != null && !image.isEmpty()) {
            b.setImageOriginalName(image.getOriginalFilename());
            b.setImageContentType(image.getContentType() != null ? image.getContentType() : "application/octet-stream");
            try {
                b.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException("이미지 업로드 중 오류가 발생했습니다");
            }
        }
        return boardRepository.save(b);
    }

    @Override
    @Transactional
    public BoardEntity update(Long id, BoardUpdateDTO dto, String requesterId, MultipartFile image) {
        BoardEntity b = boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
        boolean requesterIsAuthor = b.getAuthor() != null && requesterId.equals(b.getAuthor().getId());
        if (!requesterIsAuthor) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다");
        }
        b.setTitle(dto.getTitle());
        b.setContent(dto.getContent());

        MemberEntity requester = requesterId != null ? memberRepository.findOneById(requesterId) : null;
        boolean isAdmin = requester != null && requester.getRole() != null && requester.getRole().contains("ADMIN");
        b.setPinned(isAdmin && dto.isPinned());

        if (image != null && !image.isEmpty()) {
            b.setImageOriginalName(image.getOriginalFilename());
            b.setImageContentType(image.getContentType() != null ? image.getContentType() : "application/octet-stream");
            try {
                b.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new IllegalStateException("이미지 업로드 중 오류가 발생했습니다");
            }
        }
        return boardRepository.save(b);
    }

    @Override
    @Transactional
    public void delete(Long id, String requesterId, boolean requesterIsAdmin) {
        BoardEntity b = boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
        boolean requesterIsAuthor = b.getAuthor() != null && requesterId.equals(b.getAuthor().getId());
        if (!(requesterIsAdmin || requesterIsAuthor)) {
            throw new IllegalStateException("삭제 권한이 없습니다");
        }
        boardRepository.delete(b);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoardEntity> list(BoardSearchDTO search) {
        List<BoardEntity> top3 = boardRepository.findTop3ByOrderByViewsDesc();
        List<Long> exclude = top3.stream().map(BoardEntity::getId).collect(Collectors.toList());
        int page = search.getPage() != null ? search.getPage() : 0;
        String type = search.getType();
        if (type == null || type.isEmpty()) {
            type = "title"; // Default to title search if not specified
        }
        try {
            return boardRepository.search(search.getQ(), type, exclude, PageRequest.of(page, 10));
        } catch (RuntimeException e) {
            return Page.empty(PageRequest.of(page, 10));
        }
    }

    @Override
    @Transactional
    public BoardEntity getAndIncreaseViews(Long id) {
        BoardEntity b = boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
        b.setViews(b.getViews() + 1);
        return boardRepository.save(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardEntity> top3ByViews() {
        return boardRepository.findTop3ByOrderByViewsDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getImageData(Long boardId) {
        BoardEntity b = boardRepository.findById(boardId).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
        return b.getImageData();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardEntity> pinnedBoards() {
        return boardRepository.findByPinnedTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public BoardEntity get(Long id) {
        return boardRepository.findById(id).orElseThrow(() -> new IllegalStateException("게시글을 찾을 수 없습니다"));
    }
}
