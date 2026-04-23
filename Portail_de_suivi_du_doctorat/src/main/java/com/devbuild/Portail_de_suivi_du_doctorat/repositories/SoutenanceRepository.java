package com.devbuild.Portail_de_suivi_du_doctorat.repositories;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Soutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SoutenanceRepository extends JpaRepository<Soutenance, Long> {

    Optional<Soutenance> findByDoctorantId(Long doctorantId);

    List<Soutenance> findByStatut(StatutSoutenance statut);

    List<Soutenance> findByDatePlanifieeBetween(LocalDate debut, LocalDate fin);

    @Query("SELECT s FROM Soutenance s WHERE s.doctorant.directeur.id = :directeurId")
    List<Soutenance> findByDirecteurId(@Param("directeurId") Long directeurId);

    @Query("SELECT s FROM Soutenance s WHERE s.statut IN ('SOUMISE','RAPPORTS_EN_ATTENTE','RAPPORTS_RECUS')")
    List<Soutenance> findEnCours();

    long countByStatut(StatutSoutenance statut);
}