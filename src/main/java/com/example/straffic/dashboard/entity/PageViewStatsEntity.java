package com.example.straffic.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PAGE_VIEW_STATS")
@Data
@NoArgsConstructor
public class PageViewStatsEntity {

    @Id
    @Column(length = 50)
    private String pageName;

    @Column(nullable = false)
    private long totalViews;
}

