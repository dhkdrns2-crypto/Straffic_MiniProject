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

@Entity
@Table(name = "ktx_train")
@Getter
@Setter
@NoArgsConstructor
public class KtxTrainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ktx_train_seq")
    @SequenceGenerator(name = "ktx_train_seq", sequenceName = "ktx_train_seq", allocationSize = 1)
    private Long id;

    private String trainNo;
    private String departure;
    private String arrival;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private Integer price;
    private Integer totalSeats;
    
    private LocalDate travelDate;
}
