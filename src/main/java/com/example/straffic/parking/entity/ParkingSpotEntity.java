package com.example.straffic.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PARKING_SPOT")
@Data
@NoArgsConstructor
public class ParkingSpotEntity {

    @Id
    @Column(length = 10)
    private String spotId; // e.g., "A-1"

    @Column(nullable = false)
    private boolean occupied;

    @Column(length = 20)
    private String plateNumber;

    @Column
    private LocalDateTime entryTime;

    public ParkingSpotEntity(String spotId) {
        this.spotId = spotId;
        this.occupied = false;
    }
}
