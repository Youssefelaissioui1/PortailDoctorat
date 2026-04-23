package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Publication;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.PublicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;
    private final DoctorantRepository doctorantRepository;

    @Override
    public Publication ajouter(Long doctorantId, Publication publication) {
        Doctorant doctorant = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        publication.setDoctorant(doctorant);
        publication.setStatut(StatutDossier.EN_ATTENTE);
        log.info("Ajout publication '{}' pour doctorant {}", publication.getTitre(), doctorantId);
        return publicationRepository.save(publication);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Publication> findById(Long id) {
        return publicationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Publication> findByDoctorantId(Long doctorantId) {
        return publicationRepository.findByDoctorantId(doctorantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Publication> findByStatut(StatutDossier statut) {
        return publicationRepository.findByStatut(statut);
    }

    @Override
    public Publication valider(Long publicationId, String commentaire) {
        Publication p = getOrThrow(publicationId);
        p.valider(commentaire);
        log.info("Publication {} validée", publicationId);
        return publicationRepository.save(p);
    }

    @Override
    public Publication rejeter(Long publicationId, String motif) {
        Publication p = getOrThrow(publicationId);
        p.rejeter(motif);
        log.info("Publication {} rejetée : {}", publicationId, motif);
        return publicationRepository.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public long countArticlesQ1Q2Valides(Long doctorantId) {
        return publicationRepository.countArticlesQ1Q2Valides(doctorantId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countConferencesValidees(Long doctorantId) {
        return publicationRepository.countConferencesValidees(doctorantId);
    }

    private Publication getOrThrow(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable : " + id));
    }
}