package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.RoleEnum;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.FormationDoctoraRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.PublicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DoctorantServiceImpl implements DoctorantService {

    private final DoctorantRepository doctorantRepository;
    private final PublicationRepository publicationRepository;
    private final FormationDoctoraRepository formationRepository;
//    private final PasswordEncoder passwordEncoder;

    @Value("${app.doctorat.duree-max-standard:3}")
    private int dureeMaxStandard;

    @Value("${app.doctorat.duree-max-absolue:6}")
    private int dureeMaxAbsolue;

    @Value("${app.doctorat.publications-min:2}")
    private int publicationsMin;

    @Value("${app.doctorat.conferences-min:2}")
    private int conferencesMin;

    @Value("${app.doctorat.heures-formation-min:200}")
    private int heuresFormationMin;

    @Override
    public Doctorant creer(Doctorant doctorant) {
//        if (doctorantRepository.existsByEmail(doctorant.getEmail())) {
//            throw new IllegalArgumentException("Email déjà utilisé : " + doctorant.getEmail());
//        }
        doctorant.setRole(RoleEnum.DOCTORANT);
//        doctorant.setMotDePasse(passwordEncoder.encode(doctorant.getMotDePasse()));
        if (doctorant.getDatePremiereInscription() == null) {
            doctorant.setDatePremiereInscription(LocalDate.now());
        }
        log.info("Création du doctorant : {}", doctorant.getNomComplet());
        return doctorantRepository.save(doctorant);
    }

    @Override
    public Doctorant modifier(Doctorant doctorant) {
        Doctorant existant = doctorantRepository.findById(doctorant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable : " + doctorant.getId()));
        existant.setNom(doctorant.getNom());
        existant.setPrenom(doctorant.getPrenom());
        existant.setSujetThese(doctorant.getSujetThese());
        existant.setLaboratoire(doctorant.getLaboratoire());
        existant.setSpecialite(doctorant.getSpecialite());
        return doctorantRepository.save(existant);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctorant> findById(Long id) {
        return doctorantRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctorant> findByCne(String cne) {
        return doctorantRepository.findByCne(cne);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Doctorant> findByEmail(String email) {
        return doctorantRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctorant> findAll() {
        return doctorantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctorant> findByDirecteurId(Long directeurId) {
        return doctorantRepository.findByDirecteurId(directeurId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctorant> findByStatut(StatutDoctorant statut) {
        return doctorantRepository.findByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifierPrerequis(Long doctorantId) {
        long articlesQ1Q2 = publicationRepository.countArticlesQ1Q2Valides(doctorantId);
        long conferences   = publicationRepository.countConferencesValidees(doctorantId);
        int  heures        = formationRepository.sumHeuresValidees(doctorantId);

        boolean ok = articlesQ1Q2 >= publicationsMin
                && conferences   >= conferencesMin
                && heures        >= heuresFormationMin;

        log.debug("Prérequis doctorant {} : articles={}/{}, confs={}/{}, heures={}/{}  → {}",
                doctorantId, articlesQ1Q2, publicationsMin,
                conferences, conferencesMin, heures, heuresFormationMin, ok);
        return ok;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean peutSeReinscrire(Long doctorantId) {
        Doctorant d = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        if (d.getDatePremiereInscription() == null) return true;
        int annees = d.getNombreAnnees();
        return annees <= dureeMaxStandard || d.getStatut() == StatutDoctorant.DEROGATION_ACCORDEE;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Doctorant> findDoctorantsEnAlerte() {
        LocalDate limite = LocalDate.now().minusYears(dureeMaxAbsolue - 1);
        return doctorantRepository.findDoctorantsProchesLimiteAbsolue(limite);
    }

    @Override
    public void changerStatut(Long doctorantId, StatutDoctorant nouveauStatut) {
        Doctorant d = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        log.info("Changement statut doctorant {} : {} → {}", doctorantId, d.getStatut(), nouveauStatut);
        d.setStatut(nouveauStatut);
        doctorantRepository.save(d);
    }

    @Override
    public void supprimerLogique(Long id) {
        Doctorant d = doctorantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        d.setActif(false);
        doctorantRepository.save(d);
    }
}