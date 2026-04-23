package com.devbuild.Portail_de_suivi_du_doctorat.Services;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.CampagneInscription;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.CampagneInscriptionRepository;
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
public class CampagneInscriptionServiceImpl implements CampagneInscriptionService {

    private final CampagneInscriptionRepository campagneRepository;

    @Override
    public CampagneInscription creer(CampagneInscription campagne) {
        campagneRepository.findByAnneeUniversitaire(campagne.getAnneeUniversitaire())
                .ifPresent(c -> { throw new IllegalStateException(
                        "Campagne déjà existante pour l'année " + campagne.getAnneeUniversitaire()); });
        log.info("Création campagne {}", campagne.getAnneeUniversitaire());
        return campagneRepository.save(campagne);
    }

    @Override
    public CampagneInscription modifier(CampagneInscription campagne) {
        CampagneInscription existante = campagneRepository.findById(campagne.getId())
                .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable"));
        existante.setDateOuverture(campagne.getDateOuverture());
        existante.setDateFermeture(campagne.getDateFermeture());
        existante.setDescription(campagne.getDescription());
        return campagneRepository.save(existante);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CampagneInscription> findById(Long id) {
        return campagneRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CampagneInscription> findCampagneActive() {
        return campagneRepository.findCampagneActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CampagneInscription> findAll() {
        return campagneRepository.findAll();
    }

    @Override
    public void activer(Long id) {
        CampagneInscription c = campagneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable"));
        c.setActive(true);
        campagneRepository.save(c);
    }

    @Override
    public void desactiver(Long id) {
        CampagneInscription c = campagneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable"));
        c.setActive(false);
        campagneRepository.save(c);
    }
}