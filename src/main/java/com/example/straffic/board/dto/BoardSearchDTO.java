package com.example.straffic.board.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class BoardSearchDTO {
    private String q;
    private String type;
    private Integer page;
}

