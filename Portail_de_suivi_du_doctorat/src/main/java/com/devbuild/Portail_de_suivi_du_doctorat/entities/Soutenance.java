package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "soutenances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"doctorant", "membresJury", "documents"})
public class Soutenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateDemande;

    private LocalDate datePlanifiee;
    private LocalTime heurePlanifiee;
    private String lieu;
    private String salleVirtuelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSoutenance statut = StatutSoutenance.BROUILLON;

    // Prérequis cochés par le système
    private boolean publisSatisfaits;
    private boolean formationSatisfaite;
    private boolean documentsComplets;

    // Autorisation admin
    private LocalDateTime dateAutorisation;
    private String commentaireAutorisation;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctorant_id", nullable = false, unique = true)
    private Doctorant doctorant;

    @OneToMany(mappedBy = "soutenance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MembreJury> membresJury = new ArrayList<>();

    @OneToMany(mappedBy = "soutenance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    public boolean tousPrerequisRemplis() {
        return publisSatisfaits && formationSatisfaite && documentsComplets;
    }

    public void soumettre() {
        if (!tousPrerequisRemplis()) {
            throw new IllegalStateException("Tous les prérequis doivent être remplis avant la soumission.");
        }
        this.statut = StatutSoutenance.SOUMISE;
        this.dateDemande = LocalDate.now();
    }

    public void autoriser(String commentaire) {
        this.statut = StatutSoutenance.AUTORISEE;
        this.dateAutorisation = LocalDateTime.now();
        this.commentaireAutorisation = commentaire;
    }

    public void planifier(LocalDate date, LocalTime heure, String lieu) {
        this.datePlanifiee = date;
        this.heurePlanifiee = heure;
        this.lieu = lieu;
        this.statut = StatutSoutenance.PLANIFIEE;
    }
}