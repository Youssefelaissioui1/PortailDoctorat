package com.devbuild.Portail_de_suivi_du_doctorat.entities;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.QuartileEnum;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypePublication;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;

@Entity
@Table(name = "publications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "doctorant")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(nullable = false, length = 500)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePublication type;

    @Enumerated(EnumType.STRING)
    private QuartileEnum quartile;

    private String revueOuConference;
    private String doi;
    private LocalDate datePublication;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDossier statut = StatutDossier.EN_ATTENTE;

    private String commentaireValidation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctorant_id", nullable = false)
    private Doctorant doctorant;

    public boolean isJournalQ1Q2() {
        return type == TypePublication.ARTICLE_JOURNAL
                && (quartile == QuartileEnum.Q1 || quartile == QuartileEnum.Q2);
    }

    public void valider(String commentaire) {
        this.statut = StatutDossier.VALIDE;
        this.commentaireValidation = commentaire;
    }

    public void rejeter(String motif) {
        this.statut = StatutDossier.REJETE;
        this.commentaireValidation = motif;
    }
}