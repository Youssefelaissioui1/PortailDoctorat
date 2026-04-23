package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.CampagneInscription;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Inscription;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypeInscription;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.CampagneInscriptionRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InscriptionServiceImpl implements InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final DoctorantRepository doctorantRepository;
    private final CampagneInscriptionRepository campagneRepository;
    private final com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService doctorantService;

    @Override
    public Inscription soumettre(Long doctorantId, Long campagneId, Inscription inscription) {
        Doctorant doctorant = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));

        int anneeEnCours = LocalDate.now().getYear();

        // Vérification : pas de double inscription pour la même année
        if (inscriptionRepository.existsByDoctorantIdAndAnneeUniversitaire(doctorantId, anneeEnCours)) {
            throw new IllegalStateException("Une inscription existe déjà pour l'année " + anneeEnCours);
        }

        // Vérification durée doctorat
        if (!doctorantService.peutSeReinscrire(doctorantId)) {
            throw new IllegalStateException("Le doctorant dépasse la durée autorisée sans dérogation.");
        }

        // Campagne
        CampagneInscription campagne = campagneRepository.findById(campagneId)
                .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable"));
        if (!campagne.isOuverte()) {
            throw new IllegalStateException("La campagne d'inscription n'est pas ouverte.");
        }

        // Type : première inscription ou réinscription
        boolean premiereInscription = inscriptionRepository.findByDoctorantId(doctorantId).isEmpty();
        inscription.setType(premiereInscription ? TypeInscription.PREMIERE_INSCRIPTION : TypeInscription.REINSCRIPTION);
        inscription.setDoctorant(doctorant);
        inscription.setCampagne(campagne);
        inscription.setAnneeUniversitaire(anneeEnCours);
        inscription.setStatut(StatutDossier.EN_VALIDATION_DIRECTEUR);
        inscription.setDateDepot(LocalDate.now());

        log.info("Soumission inscription doctorant {} - type {}", doctorantId, inscription.getType());
        return inscriptionRepository.save(inscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inscription> findById(Long id) {
        return inscriptionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findByDoctorantId(Long doctorantId) {
        return inscriptionRepository.findByDoctorantId(doctorantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findByStatut(StatutDossier statut) {
        return inscriptionRepository.findByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findEnAttenteAvisDirecteur(Long directeurId) {
        return inscriptionRepository.findEnAttenteAvisDirecteur(directeurId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inscription> findEnAttenteValidationAdmin() {
        return inscriptionRepository.findEnAttenteValidationAdmin();
    }

    @Override
    public Inscription validerParDirecteur(Long inscriptionId, String avis) {
        Inscription i = getOrThrow(inscriptionId);
        if (i.getStatut() != StatutDossier.EN_VALIDATION_DIRECTEUR) {
            throw new IllegalStateException("L'inscription n'est pas en attente d'avis directeur.");
        }
        i.validerParDirecteur(avis);
        log.info("Avis directeur pour inscription {} : {}", inscriptionId, avis);
        return inscriptionRepository.save(i);
    }

    @Override
    public Inscription validerParAdmin(Long inscriptionId, String commentaire) {
        Inscription i = getOrThrow(inscriptionId);
        if (i.getStatut() != StatutDossier.EN_VALIDATION_ADMIN) {
            throw new IllegalStateException("L'inscription n'est pas en attente de validation admin.");
        }
        i.validerParAdmin(commentaire);
        // Mettre à jour l'année en cours du doctorant
        i.getDoctorant().setAnneeEncours(i.getAnneeUniversitaire() - i.getDoctorant().getDatePremiereInscription().getYear() + 1);
        log.info("Inscription {} validée par admin", inscriptionId);
        return inscriptionRepository.save(i);
    }

    @Override
    public Inscription rejeter(Long inscriptionId, String motif) {
        Inscription i = getOrThrow(inscriptionId);
        i.rejeter(motif);
        log.info("Inscription {} rejetée : {}", inscriptionId, motif);
        return inscriptionRepository.save(i);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatut(StatutDossier statut) {
        return inscriptionRepository.countByStatut(statut);
    }

    private Inscription getOrThrow(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable : " + id));
    }
}