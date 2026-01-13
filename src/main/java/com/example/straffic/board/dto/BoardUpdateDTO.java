package com.example.straffic.board.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardUpdateDTO {
    private String title;
    private String content;
    private boolean pinned;
}

