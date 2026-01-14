package com.example.straffic.mobility.repository;

import com.example.straffic.mobility.entity.KtxTrainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KtxTrainRepository extends JpaRepository<KtxTrainEntity, Long> {
    List<KtxTrainEntity> findByDepartureAndArrivalAndTravelDate(String departure, String arrival, LocalDate travelDate);
}
