package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "court_name", nullable = false)
    private String courtName;

    @Column(nullable = false)
    private String sport;

    @Column(name = "court_location")
    private String courtLocation;

    @Column(name = "courts_available")
    private Integer courtsAvailable;

    @Column
    private Integer price;

    @Column(name = "court_image")
    private String courtImage;

    @Column(name = "game_icon")
    private String gameIcon;

    @Column
    private BigDecimal rating;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "disabled", nullable = false)
    private Boolean disabled = false;
}
