package com.devbuild.Portail_de_suivi_du_doctorat.Services;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Publication;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;


import java.util.List;
import java.util.Optional;

public interface PublicationService {

    Publication ajouter(Long doctorantId, Publication publication);

    Optional<Publication> findById(Long id);

    List<Publication> findByDoctorantId(Long doctorantId);

    List<Publication> findByStatut(StatutDossier statut);

    Publication valider(Long publicationId, String commentaire);

    Publication rejeter(Long publicationId, String motif);

    long countArticlesQ1Q2Valides(Long doctorantId);

    long countConferencesValidees(Long doctorantId);
}