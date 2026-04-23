package com.devbuild.Portail_de_suivi_du_doctorat.Services;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.MembreJury;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Soutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface SoutenanceService {

    Soutenance creerDemande(Long doctorantId);

    Soutenance soumettre(Long soutenanceId);

    Optional<Soutenance> findById(Long id);

    Optional<Soutenance> findByDoctorantId(Long doctorantId);

    List<Soutenance> findByStatut(StatutSoutenance statut);

    List<Soutenance> findEnCours();

    /** Directeur propose un membre du jury */
    MembreJury ajouterMembreJury(Long soutenanceId, MembreJury membre);

    /** Rapporteur soumet son rapport */
    void soumettreRapportJury(Long membreJuryId, boolean favorable);

    /** Admin autorise la soutenance */
    Soutenance autoriser(Long soutenanceId, String commentaire);

    /** Admin planifie la date/lieu */
    Soutenance planifier(Long soutenanceId, LocalDate date, LocalTime heure, String lieu);

    long countByStatut(StatutSoutenance statut);
}