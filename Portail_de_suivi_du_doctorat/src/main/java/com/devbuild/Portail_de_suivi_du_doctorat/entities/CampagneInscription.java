package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campagnes_inscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "inscriptions")
public class CampagneInscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private int anneeUniversitaire;

    @NonNull
    private LocalDate dateOuverture;

    @NonNull
    private LocalDate dateFermeture;

    @Column(nullable = false)
    private boolean active = false;

    private String description;

    @OneToMany(mappedBy = "campagne", fetch = FetchType.LAZY)
    private List<Inscription> inscriptions = new ArrayList<>();

    public boolean isOuverte() {
        LocalDate today = LocalDate.now();
        return active && !today.isBefore(dateOuverture) && !today.isAfter(dateFermeture);
    }
}