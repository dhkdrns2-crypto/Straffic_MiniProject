package com.example.straffic.dashboard.service;

import com.example.straffic.dashboard.dto.DailyViewsDTO;
import com.example.straffic.dashboard.entity.PageViewHistoryEntity;
import com.example.straffic.dashboard.entity.PageViewStatsEntity;
import com.example.straffic.dashboard.repository.PageViewHistoryRepository;
import com.example.straffic.dashboard.repository.PageViewStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PageViewStatsService {

    private final PageViewStatsRepository repository;
    private final PageViewHistoryRepository historyRepository;

    @Transactional
    public void increaseView(String pageName) {
        // Update total count
        PageViewStatsEntity entity = repository.findById(pageName)
                .orElseGet(() -> {
                    PageViewStatsEntity e = new PageViewStatsEntity();
                    e.setPageName(pageName);
                    e.setTotalViews(0L);
                    return e;
                });
        entity.setTotalViews(entity.getTotalViews() + 1);
        repository.save(entity);

        // Save history
        PageViewHistoryEntity history = new PageViewHistoryEntity();
        history.setPageName(pageName);
        history.setViewedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public DailyViewsDTO getTotals() {
        long parking = getTotalViews("PARKING");
        long ktx = getTotalViews("KTX");
        long bike = getTotalViews("WAYFINDING");
        long subway = getTotalViews("SUBWAY");
        return new DailyViewsDTO(
                (int) parking,
                (int) ktx,
                (int) bike,
                (int) subway
        );
    }

    @Transactional(readOnly = true)
    public long getTotalViews(String pageName) {
        return repository.findById(pageName)
                .map(PageViewStatsEntity::getTotalViews)
                .orElse(0L);
    }
}

