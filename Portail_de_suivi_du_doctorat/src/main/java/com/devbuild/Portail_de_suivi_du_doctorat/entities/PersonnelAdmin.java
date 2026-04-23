package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import com.devbuild.Portail_de_suivi_du_doctorat.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personnel_admins")
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class PersonnelAdmin extends Utilisateur {

    private String matricule;
    private String service;
    private String poste;

    @Builder
    public PersonnelAdmin(String nom, String prenom, String email, String motDePasse,
                          String matricule, String service, String poste) {
        super(null, nom, prenom, email, motDePasse, RoleEnum.PERSONNEL_ADMIN, true);
        this.matricule = matricule;
        this.service = service;
        this.poste = poste;
    }
}