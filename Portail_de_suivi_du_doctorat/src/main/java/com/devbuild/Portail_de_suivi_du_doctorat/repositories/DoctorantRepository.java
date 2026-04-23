package com.devbuild.Portail_de_suivi_du_doctorat.repositories;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.DirecteurThese;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorantRepository extends JpaRepository<Doctorant, Long> {

    Optional<Doctorant> findByCne(String cne);

    Optional<Doctorant> findByEmail(String email);

    List<Doctorant> findByDirecteur(DirecteurThese directeur);

    List<Doctorant> findByStatut(StatutDoctorant statut);

    List<Doctorant> findByDirecteurId(Long directeurId);

    /** Doctorants dont la date de 1ère inscription dépasse les N années autorisées */
    @Query("SELECT d FROM Doctorant d WHERE d.datePremiereInscription <= :dateLimite AND d.statut = 'ACTIF'")
    List<Doctorant> findDoctorantsEnDepassement(@Param("dateLimite") LocalDate dateLimite);

    /** Doctorants proches de la limite (alerte 6 ans) */
    @Query("SELECT d FROM Doctorant d WHERE d.datePremiereInscription <= :dateLimite AND d.statut <> 'DIPLOME'")
    List<Doctorant> findDoctorantsProchesLimiteAbsolue(@Param("dateLimite") LocalDate dateLimite);

}