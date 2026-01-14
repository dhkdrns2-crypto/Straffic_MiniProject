package com.example.straffic.mobility.repository;

import com.example.straffic.mobility.entity.SubwayStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubwayStationRepository extends JpaRepository<SubwayStationEntity, Long> {

    List<SubwayStationEntity> findByLineNumber(String lineNumber);

    List<SubwayStationEntity> findTop20ByStationNameContaining(String keyword);

    long countByLineNumberIn(List<String> lineNumbers);
}
