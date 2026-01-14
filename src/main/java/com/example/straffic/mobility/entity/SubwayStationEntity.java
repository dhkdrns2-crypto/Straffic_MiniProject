package com.example.straffic.mobility.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SUBWAY_STATION")
@Getter
@Setter
@NoArgsConstructor
public class SubwayStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subway_station_seq")
    @SequenceGenerator(name = "subway_station_seq", sequenceName = "SUBWAY_STATION_SEQ", allocationSize = 1)
    private Long stationId;

    private String stationName;

    private String lineNumber;

    private Double latitude;

    private Double longitude;

    private String transferLines;
}

