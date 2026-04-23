package com.devbuild.Portail_de_suivi_du_doctorat.entities;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypeDocument;
import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"inscription", "soutenance"})
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeDocument type;

    @NotNull
    private String url;

    private String format;
    private long taille;

    @Column(nullable = false)
    private LocalDateTime dateTelechargement = LocalDateTime.now();

    private boolean conforme = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inscription_id")
    private Inscription inscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "soutenance_id")
    private Soutenance soutenance;

    // Formats acceptés
    private static final Set<String> FORMATS_ACCEPTES = Set.of("pdf", "jpg", "jpeg", "png");

    public boolean verifierFormat() {
        if (format == null) return false;
        return FORMATS_ACCEPTES.contains(format.toLowerCase());
    }

    public void marquerConforme() {
        this.conforme = true;
    }
}