package com.isep.appli.dbModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class JoinRequest {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnage_id", nullable = false, foreignKey = @ForeignKey(name = "none"))
    private Personnage personnage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "familia_id", nullable = false, foreignKey = @ForeignKey(name = "none"))
    private Familia familia;

    @Column(nullable = false)
    private boolean accepted = false;
}

