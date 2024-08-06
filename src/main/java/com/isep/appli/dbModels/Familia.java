package com.isep.appli.dbModels;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Familia {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String embleme_image;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "familia")
    private List<Personnage> personnages;

    @Override
    public String toString() {
        return "Familia{" +
                "id=" + id +
                ", description='" + description + '\'' +
                //", embleme_image='" + embleme_image + '\'' +
                '}';
    }
}