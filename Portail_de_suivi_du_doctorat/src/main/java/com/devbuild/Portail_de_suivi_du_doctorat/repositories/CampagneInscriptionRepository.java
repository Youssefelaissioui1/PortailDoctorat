package com.devbuild.Portail_de_suivi_du_doctorat.repositories;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.CampagneInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampagneInscriptionRepository extends JpaRepository<CampagneInscription, Long> {

    Optional<CampagneInscription> findByAnneeUniversitaire(int annee);

    @Query("SELECT c FROM CampagneInscription c WHERE c.active = true ORDER BY c.anneeUniversitaire DESC")
    Optional<CampagneInscription> findCampagneActive();
}