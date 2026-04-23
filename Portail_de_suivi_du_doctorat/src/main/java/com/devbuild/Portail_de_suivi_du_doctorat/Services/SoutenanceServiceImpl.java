package com.devbuild.Portail_de_suivi_du_doctorat.Services;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.MembreJury;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Soutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DocumentRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.MembreJuryRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.SoutenanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SoutenanceServiceImpl implements SoutenanceService {

    private final SoutenanceRepository soutenanceRepository;
    private final DoctorantRepository doctorantRepository;
    private final MembreJuryRepository membreJuryRepository;
    private final com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService doctorantService;

    @Override
    public Soutenance creerDemande(Long doctorantId) {
        Doctorant doctorant = doctorantRepository.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));
        if (soutenanceRepository.findByDoctorantId(doctorantId).isPresent()) {
            throw new IllegalStateException("Une demande de soutenance existe déjà pour ce doctorant.");
        }

        // Vérifier les prérequis
        boolean prerequis = doctorantService.verifierPrerequis(doctorantId);

        Soutenance soutenance = Soutenance.builder()
                .doctorant(doctorant)
                .dateDemande(LocalDate.now())
                .statut(StatutSoutenance.BROUILLON)
                .publisSatisfaits(prerequis)
                .formationSatisfaite(prerequis)
                .documentsComplets(false)
                .build();

        log.info("Demande soutenance créée pour doctorant {}", doctorantId);
        return soutenanceRepository.save(soutenance);
    }

    @Override
    public Soutenance soumettre(Long soutenanceId) {
        Soutenance s = getOrThrow(soutenanceId);
        s.soumettre(); // lève IllegalStateException si prérequis manquants
        log.info("Soutenance {} soumise", soutenanceId);
        return soutenanceRepository.save(s);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Soutenance> findById(Long id) {
        return soutenanceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Soutenance> findByDoctorantId(Long doctorantId) {
        return soutenanceRepository.findByDoctorantId(doctorantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Soutenance> findByStatut(StatutSoutenance statut) {
        return soutenanceRepository.findByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Soutenance> findEnCours() {
        return soutenanceRepository.findEnCours();
    }

    @Override
    public MembreJury ajouterMembreJury(Long soutenanceId, MembreJury membre) {
        Soutenance soutenance = getOrThrow(soutenanceId);
        membre.setSoutenance(soutenance);
        log.info("Ajout membre jury {} ({}) à soutenance {}",
                membre.getNomComplet(), membre.getQualite(), soutenanceId);
        return membreJuryRepository.save(membre);
    }

    @Override
    public void soumettreRapportJury(Long membreJuryId, boolean favorable) {
        MembreJury membre = membreJuryRepository.findById(membreJuryId)
                .orElseThrow(() -> new IllegalArgumentException("Membre jury introuvable"));
        membre.soumettreRapport(favorable);
        membreJuryRepository.save(membre);

        // Vérifier si tous les rapporteurs ont soumis
        Soutenance soutenance = membre.getSoutenance();
        boolean tousRapportsRecus = soutenance.getMembresJury().stream()
                .filter(m -> m.getQualite().name().contains("RAPPORTEUR"))
                .allMatch(MembreJury::isRapportSoumis);

        if (tousRapportsRecus) {
            soutenance.setStatut(StatutSoutenance.RAPPORTS_RECUS);
            soutenanceRepository.save(soutenance);
            log.info("Tous les rapports reçus pour soutenance {}", soutenance.getId());
        }
    }

    @Override
    public Soutenance autoriser(Long soutenanceId, String commentaire) {
        Soutenance s = getOrThrow(soutenanceId);
        s.autoriser(commentaire);
        log.info("Soutenance {} autorisée", soutenanceId);
        return soutenanceRepository.save(s);
    }

    @Override
    public Soutenance planifier(Long soutenanceId, LocalDate date, LocalTime heure, String lieu) {
        Soutenance s = getOrThrow(soutenanceId);
        if (s.getStatut() != StatutSoutenance.AUTORISEE) {
            throw new IllegalStateException("La soutenance doit être autorisée avant d'être planifiée.");
        }
        s.planifier(date, heure, lieu);
        log.info("Soutenance {} planifiée le {} à {} en {}", soutenanceId, date, heure, lieu);
        return soutenanceRepository.save(s);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatut(StatutSoutenance statut) {
        return soutenanceRepository.countByStatut(statut);
    }

    private Soutenance getOrThrow(Long id) {
        return soutenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Soutenance introuvable : " + id));
    }
}
