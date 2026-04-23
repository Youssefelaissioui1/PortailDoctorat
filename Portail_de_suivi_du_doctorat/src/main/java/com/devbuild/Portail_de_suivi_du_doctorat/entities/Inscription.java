package com.devbuild.Portail_de_suivi_du_doctorat.entities;


import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypeInscription;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"doctorant", "documents"})
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeInscription type;

    @Column(nullable = false)
    private int anneeUniversitaire;

    @Column(nullable = false)
    private LocalDate dateDepot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut = StatutDossier.SOUMIS;

    private String motifRejet;

    // Avis directeur
    private String avisDirecteur;
    private LocalDateTime dateAvisDirecteur;

    // Validation admin
    private String commentaireAdmin;
    private LocalDateTime dateValidationAdmin;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctorant_id", nullable = false)
    private Doctorant doctorant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id")
    private CampagneInscription campagne;

    @OneToMany(mappedBy = "inscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    /** Passage à l'état suivant */
    public void soumettre() {
        this.statut = StatutDossier.SOUMIS;
        this.dateDepot = LocalDate.now();
    }

    public void validerParDirecteur(String avis) {
        this.avisDirecteur = avis;
        this.dateAvisDirecteur = LocalDateTime.now();
        this.statut = StatutDossier.EN_VALIDATION_ADMIN;
    }

    public void validerParAdmin(String commentaire) {
        this.commentaireAdmin = commentaire;
        this.dateValidationAdmin = LocalDateTime.now();
        this.statut = StatutDossier.VALIDE;
    }

    public void rejeter(String motif) {
        this.motifRejet = motif;
        this.statut = StatutDossier.REJETE;
    }
}