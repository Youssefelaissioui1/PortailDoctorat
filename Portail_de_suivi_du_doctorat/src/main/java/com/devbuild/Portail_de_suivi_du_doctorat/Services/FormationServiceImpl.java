package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.FormationDoctorate;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.FormationDoctoraRepository;
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
public class FormationServiceImpl implements FormationService {

    private final FormationDoctoraRepository formationRepository;
    private final DoctorantRepository doctorantRepository;

    @Override
    public FormationDoctorate ajouter(Long doctorantId, FormationDoctorate formation) {
        Doctorant doctorant = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        formation.setDoctorant(doctorant);
        formation.setValidee(false);
        log.info("Ajout formation '{}' ({} h) pour doctorant {}", formation.getIntitule(), formation.getHeures(), doctorantId);
        return formationRepository.save(formation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FormationDoctorate> findById(Long id) {
        return formationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormationDoctorate> findByDoctorantId(Long doctorantId) {
        return formationRepository.findByDoctorantId(doctorantId);
    }

    @Override
    public FormationDoctorate valider(Long formationId) {
        FormationDoctorate f = formationRepository.findById(formationId)
                .orElseThrow(() -> new IllegalArgumentException("Formation introuvable : " + formationId));
        f.valider();
        log.info("Formation {} validée", formationId);
        return formationRepository.save(f);
    }

    @Override
    @Transactional(readOnly = true)
    public int getTotalHeuresValidees(Long doctorantId) {
        return formationRepository.sumHeuresValidees(doctorantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormationDoctorate> findNonValidees() {
        return formationRepository.findByValidee(false);
    }
}