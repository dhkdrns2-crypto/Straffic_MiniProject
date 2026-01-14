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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ktx_reservation")
@Getter
@Setter
@NoArgsConstructor
public class KtxReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ktx_reservation_seq")
    @SequenceGenerator(name = "ktx_reservation_seq", sequenceName = "ktx_reservation_seq", allocationSize = 1)
    private Long id;

    private String memberId;
    private Integer passengerCount;

    private String trainNo;
    private String departure;
    private String arrival;
    private String departureTime;
    private String arrivalTime;

    private LocalDate travelDate;

    private String seats;
    private Integer seatCount;
    private Integer totalPrice;

    private LocalDateTime reservedAt;
}
