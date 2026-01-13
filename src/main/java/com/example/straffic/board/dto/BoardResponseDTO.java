package com.example.straffic.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BoardResponseDTO {
    private Long id;
    private String title;
    private String authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private long views;
    private boolean pinned;
}

