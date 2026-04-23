package com.devbuild.Portail_de_suivi_du_doctorat.entities;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.QualiteJury;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "membres_jury")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "soutenance")
public class MembreJury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nom;

    @NotNull
    private String prenom;

    private String email;
    private String grade;

    @NotNull
    private String etablissement;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QualiteJury qualite;

    // Pour les rapporteurs
    private boolean rapportSoumis = false;
    private boolean rapportFavorable = false;
    private LocalDate dateSoumissionRapport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soutenance_id", nullable = false)
    private Soutenance soutenance;

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public void soumettreRapport(boolean favorable) {
        this.rapportSoumis = true;
        this.rapportFavorable = favorable;
        this.dateSoumissionRapport = LocalDate.now();
    }
}