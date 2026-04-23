package com.devbuild.Portail_de_suivi_du_doctorat.repositories;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Inscription;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByDoctorantId(Long doctorantId);

    List<Inscription> findByStatut(StatutDossier statut);

    List<Inscription> findByAnneeUniversitaire(int annee);

    Optional<Inscription> findByDoctorantIdAndAnneeUniversitaire(Long doctorantId, int annee);

    boolean existsByDoctorantIdAndAnneeUniversitaire(Long doctorantId, int annee);

    @Query("SELECT i FROM Inscription i WHERE i.doctorant.directeur.id = :directeurId AND i.statut = 'EN_VALIDATION_DIRECTEUR'")
    List<Inscription> findEnAttenteAvisDirecteur(@Param("directeurId") Long directeurId);

    @Query("SELECT i FROM Inscription i WHERE i.statut = 'EN_VALIDATION_ADMIN'")
    List<Inscription> findEnAttenteValidationAdmin();

    long countByStatut(StatutDossier statut);
}