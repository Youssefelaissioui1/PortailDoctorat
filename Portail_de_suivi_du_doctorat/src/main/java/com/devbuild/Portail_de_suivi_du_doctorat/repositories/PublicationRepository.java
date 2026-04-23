package com.devbuild.Portail_de_suivi_du_doctorat.repositories;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Publication;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypePublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    List<Publication> findByDoctorantId(Long doctorantId);

    List<Publication> findByDoctorantIdAndStatut(Long doctorantId, StatutDossier statut);

    List<Publication> findByDoctorantIdAndType(Long doctorantId, TypePublication type);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.doctorant.id = :doctorantId " +
            "AND p.type = 'ARTICLE_JOURNAL' AND p.quartile IN ('Q1','Q2') AND p.statut = 'VALIDE'")
    long countArticlesQ1Q2Valides(@Param("doctorantId") Long doctorantId);

    @Query("SELECT COUNT(p) FROM Publication p WHERE p.doctorant.id = :doctorantId " +
            "AND p.type = 'CONFERENCE' AND p.statut = 'VALIDE'")
    long countConferencesValidees(@Param("doctorantId") Long doctorantId);

    List<Publication> findByStatut(StatutDossier statut);
}