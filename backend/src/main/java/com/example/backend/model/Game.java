package com.example.backend.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "sport_icon")
    private String sportIcon;

    @Column(name = "sport_name")
    private String sportName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;
    
    @Column(name = "members_joined")
    private Integer membersJoined;

    @Column(name = "total_members")
    private Integer totalMembers;

    @Column(name = "skill_level")
    private String skillLevel;

    @Column(name = "court_name")
    private String courtName;

    @Column(name = "location")
    private String location;

}
