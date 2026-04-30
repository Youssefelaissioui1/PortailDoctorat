package com.devbuild.Portail_de_suivi_du_doctorat.Services;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.FormationDoctorate;

import java.util.List;
import java.util.Optional;

public interface FormationService {

    FormationDoctorate ajouter(Long doctorantId, FormationDoctorate formation);

    Optional<FormationDoctorate> findById(Long id);

    List<FormationDoctorate> findByDoctorantId(Long doctorantId);

    FormationDoctorate valider(Long formationId);

    int getTotalHeuresValidees(Long doctorantId);

    List<FormationDoctorate> findNonValidees();
}