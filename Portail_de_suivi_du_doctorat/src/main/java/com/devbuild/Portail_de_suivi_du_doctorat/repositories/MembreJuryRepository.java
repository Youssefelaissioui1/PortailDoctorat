package com.devbuild.Portail_de_suivi_du_doctorat.repositories;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.MembreJury;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.QualiteJury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembreJuryRepository extends JpaRepository<MembreJury, Long> {

    List<MembreJury> findBySoutenanceId(Long soutenanceId);

    List<MembreJury> findBySoutenanceIdAndQualite(Long soutenanceId, QualiteJury qualite);

    boolean existsBySoutenanceIdAndQualite(Long soutenanceId, QualiteJury qualite);
}