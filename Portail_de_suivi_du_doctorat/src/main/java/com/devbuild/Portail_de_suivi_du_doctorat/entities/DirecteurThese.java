package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import com.devbuild.Portail_de_suivi_du_doctorat.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "directeurs_these")
@DiscriminatorValue("DIRECTEUR")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = "doctorants")
public class DirecteurThese extends Utilisateur {

    private String specialite;
    private String grade;
    private String etablissement;

    @OneToMany(mappedBy = "directeur", fetch = FetchType.LAZY)
    private List<Doctorant> doctorants = new ArrayList<>();

    @Builder
    public DirecteurThese(String nom, String prenom, String email, String motDePasse,
                          String specialite, String grade, String etablissement) {
        super(null, nom, prenom, email, motDePasse, RoleEnum.DIRECTEUR_THESE, true);
        this.specialite = specialite;
        this.grade = grade;
        this.etablissement = etablissement;
    }
}