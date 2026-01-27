package com.example.straffic.dashboard.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAGE_VIEW_HISTORY")
@Data
@NoArgsConstructor
public class PageViewHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pageName;

    @Column(nullable = false)
    private LocalDateTime viewedAt;
}
