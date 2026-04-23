package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "formations_doctorales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "doctorant")
public class FormationDoctorate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false)
    private String intitule;

//    @Min(1)
    @Column(nullable = false)
    private int heures;

    private LocalDate dateFormation;
    private LocalDate dateValidation;
    private String organisateur;
    private String attestationUrl;

    @Column(nullable = false)
    private boolean validee = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctorant_id", nullable = false)
    private Doctorant doctorant;

    public void valider() {
        this.validee = true;
        this.dateValidation = LocalDate.now();
    }
}