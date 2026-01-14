package com.example.straffic.mobility.repository;

import com.example.straffic.mobility.entity.KtxReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface KtxReservationRepository extends JpaRepository<KtxReservationEntity, Long> {

    List<KtxReservationEntity> findByTrainNoAndTravelDate(String trainNo, LocalDate travelDate);
}

