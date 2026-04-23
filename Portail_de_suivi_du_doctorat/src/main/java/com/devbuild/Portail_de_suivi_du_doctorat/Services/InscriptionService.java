package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Inscription;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;


import java.util.List;
import java.util.Optional;

public interface InscriptionService {

    Inscription soumettre(Long doctorantId, Long campagneId, Inscription inscription);

    Optional<Inscription> findById(Long id);

    List<Inscription> findByDoctorantId(Long doctorantId);

    List<Inscription> findByStatut(StatutDossier statut);

    List<Inscription> findEnAttenteAvisDirecteur(Long directeurId);

    List<Inscription> findEnAttenteValidationAdmin();

    /** Directeur donne son avis */
    Inscription validerParDirecteur(Long inscriptionId, String avis);

    /** Admin valide définitivement */
    Inscription validerParAdmin(Long inscriptionId, String commentaire);

    /** Admin rejette */
    Inscription rejeter(Long inscriptionId, String motif);

    long countByStatut(StatutDossier statut);
}