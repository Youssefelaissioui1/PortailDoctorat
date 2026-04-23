package com.devbuild.Portail_de_suivi_du_doctorat.Services;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.CampagneInscription;

import java.util.List;
import java.util.Optional;

public interface CampagneInscriptionService {

    CampagneInscription creer(CampagneInscription campagne);

    CampagneInscription modifier(CampagneInscription campagne);

    Optional<CampagneInscription> findById(Long id);

    Optional<CampagneInscription> findCampagneActive();

    List<CampagneInscription> findAll();

    void activer(Long id);

    void desactiver(Long id);
}