package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;


import java.util.List;
import java.util.Optional;

public interface DoctorantService {

    Doctorant creer(Doctorant doctorant);

    Doctorant modifier(Doctorant doctorant);

    Optional<Doctorant> findById(Long id);

    Optional<Doctorant> findByCne(String cne);

    Optional<Doctorant> findByEmail(String email);

    List<Doctorant> findAll();

    List<Doctorant> findByDirecteurId(Long directeurId);

    List<Doctorant> findByStatut(StatutDoctorant statut);

    /** Vérifie les prérequis à la soutenance (publications, formations) */
    boolean verifierPrerequis(Long doctorantId);

    /** Vérifie si le doctorant peut se réinscrire (règle des 3 / 6 ans) */
     boolean peutSeReinscrire(Long doctorantId);

    /** Retourne les doctorants dépassant la durée standard (alerte) */
    List<Doctorant> findDoctorantsEnAlerte();

    void changerStatut(Long doctorantId, StatutDoctorant nouveauStatut);

    void supprimerLogique(Long id);
}