package com.devbuild.Portail_de_suivi_du_doctorat.entities;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.RoleEnum;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypePublication;
import jakarta.persistence.*;
import lombok.*;

import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctorants")
@DiscriminatorValue("DOCTORANT")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, exclude = {"inscriptions", "soutenance", "publications", "formations"})
public class Doctorant extends Utilisateur {

    @NotNull
    @Column(nullable = false, unique = true)
    private String cne;

    @NotNull
    @Column(nullable = false, length = 500)
    private String sujetThese;

    @Column(nullable = false)
    private LocalDate datePremiereInscription;

    @Column(nullable = false)
    private int anneeEncours = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDoctorant statut = StatutDoctorant.ACTIF;

    private String laboratoire;
    private String specialite;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directeur_id")
    private DirecteurThese directeur;

    @OneToMany(mappedBy = "doctorant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscription> inscriptions = new ArrayList<>();

    @OneToOne(mappedBy = "doctorant", cascade = CascadeType.ALL)
    private Soutenance soutenance;

    @OneToMany(mappedBy = "doctorant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Publication> publications = new ArrayList<>();

    @OneToMany(mappedBy = "doctorant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormationDoctorate> formations = new ArrayList<>();

    @Builder
    public Doctorant(String nom, String prenom, String email, String motDePasse,
                     String cne, String sujetThese, LocalDate datePremiereInscription,
                     DirecteurThese directeur) {
        super(null, nom, prenom, email, motDePasse, RoleEnum.DOCTORANT, true);
        this.cne = cne;
        this.sujetThese = sujetThese;
        this.datePremiereInscription = datePremiereInscription;
        this.directeur = directeur;
    }

    /** Nombre d'années depuis la première inscription */
    public int getNombreAnnees() {
        if (datePremiereInscription == null) return 0;
        return LocalDate.now().getYear() - datePremiereInscription.getYear() + 1;
    }

    /** Nombre de publications validées de type journal Q1/Q2 */
    public long getNbArticlesQ1Q2() {
        return publications.stream()
                .filter(p -> p.getStatut() == StatutDossier.VALIDE)
                .filter(Publication::isJournalQ1Q2)
                .count();
    }

    /** Nombre de conférences validées */
    public long getNbConferences() {
        return publications.stream()
                .filter(p -> p.getStatut() == StatutDossier.VALIDE)
                .filter(p -> p.getType() == TypePublication.CONFERENCE)
                .count();
    }

    /** Total heures de formation validées */
    public int getTotalHeuresFormation() {
        return formations.stream()
                .filter(f -> f.isValidee())
                .mapToInt(FormationDoctorate::getHeures)
                .sum();
    }
}