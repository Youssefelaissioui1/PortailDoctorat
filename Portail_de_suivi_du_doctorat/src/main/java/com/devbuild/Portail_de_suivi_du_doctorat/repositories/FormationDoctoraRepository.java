package com.devbuild.Portail_de_suivi_du_doctorat.repositories;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.FormationDoctorate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormationDoctoraRepository extends JpaRepository<FormationDoctorate, Long> {

    List<FormationDoctorate> findByDoctorantId(Long doctorantId);

    List<FormationDoctorate> findByDoctorantIdAndValidee(Long doctorantId, boolean validee);

    @Query("SELECT COALESCE(SUM(f.heures), 0) FROM FormationDoctorate f " +
            "WHERE f.doctorant.id = :doctorantId AND f.validee = true")
    int sumHeuresValidees(@Param("doctorantId") Long doctorantId);

    List<FormationDoctorate> findByValidee(boolean validee);}