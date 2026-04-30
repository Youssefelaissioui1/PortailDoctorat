package com.devbuild.Portail_de_suivi_du_doctorat.Controller;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.FormationService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.InscriptionService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.PublicationService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.SoutenanceService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Tableau de bord global de l'administration.
 *
 * ══════════════════════════════════════════════════════════
 * MÉTHODES DE SERVICE COUVERTES (toutes celles restantes)
 * ══════════════════════════════════════════════════════════
 * DoctorantService :
 *   ✔ findAll()                    → liste complète des doctorants
 *   ✔ findById(id)                 → fiche d'un doctorant
 *   ✔ findByStatut(statut)         → filtrer par statut
 *   ✔ findByDirecteurId(id)        → doctorants d'un directeur
 *   ✔ findDoctorantsEnAlerte()     → doctorants proches de 6 ans
 *   ✔ changerStatut(id, statut)    → changer le statut (dérogation, diplômé...)
 *   ✔ supprimerLogique(id)         → désactiver un compte
 *   (findByEmail, findByCne, creer, modifier, verifierPrerequis,
 *    peutSeReinscrire → couverts dans AuthController et DoctorantController)
 *
 * InscriptionService :
 *   ✔ countByStatut()              → stats dashboard
 *   (les autres méthodes sont dans InscriptionController)
 *
 * SoutenanceService :
 *   ✔ countByStatut()              → stats dashboard
 *   ✔ findByDoctorantId()          → soutenance d'un doctorant dans sa fiche
 *   (les autres méthodes sont dans SoutenanceController)
 *
 * PublicationService :
 *   ✔ findByDoctorantId()          → publications dans la fiche doctorant
 *   ✔ countArticlesQ1Q2Valides()   → dans la fiche doctorant
 *   ✔ countConferencesValidees()   → dans la fiche doctorant
 *
 * FormationService :
 *   ✔ findByDoctorantId()          → formations dans la fiche doctorant
 *   ✔ getTotalHeuresValidees()     → dans la fiche doctorant
 *
 * ══════════════════════════════════════════════════════════
 * ROUTES
 * ══════════════════════════════════════════════════════════
 *   GET  /admin/dashboard                           → vue d'ensemble + alertes
 *   GET  /admin/doctorants                          → liste complète
 *   GET  /admin/doctorants/par-statut/{statut}      → filtrer par statut
 *   GET  /admin/doctorants/directeur/{directeurId}  → doctorants d'un directeur
 *   GET  /admin/doctorants/alertes                  → doctorants en alerte 6 ans
 *   GET  /admin/doctorants/{id}                     → fiche complète
 *   POST /admin/doctorants/{id}/statut              → changer le statut
 *   POST /admin/doctorants/{id}/supprimer           → désactivation logique
 *
 * Vues :
 *   templates/admin/dashboard.html
 *   templates/admin/doctorants.html
 *   templates/admin/doctorant-detail.html
 *   templates/admin/alertes.html
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('PERSONNEL_ADMIN')")
@RequiredArgsConstructor
public class Admincontroller {

    private final DoctorantService   doctorantService;
    private final InscriptionService inscriptionService;
    private final SoutenanceService  soutenanceService;
    private final PublicationService publicationService;
    private final FormationService   formationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // ── Doctorants ────────────────────────────────────────────────────────
        model.addAttribute("doctorantsEnAlerte",
                doctorantService.findDoctorantsEnAlerte());                 // findDoctorantsEnAlerte()
        model.addAttribute("nbDoctorantsActifs",
                doctorantService.findByStatut(StatutDoctorant.ACTIF).size()); // findByStatut()
        model.addAttribute("nbDoctorantsDerogation",
                doctorantService.findByStatut(StatutDoctorant.DEROGATION_ACCORDEE).size());
        model.addAttribute("nbDoctorantsDiplomes",
                doctorantService.findByStatut(StatutDoctorant.DIPLOME).size());

        // ── Inscriptions ──────────────────────────────────────────────────────
        model.addAttribute("nbInscriptionsAttenteDirecteur",
                inscriptionService.countByStatut(StatutDossier.EN_VALIDATION_DIRECTEUR)); // countByStatut()
        model.addAttribute("nbInscriptionsAttenteAdmin",
                inscriptionService.countByStatut(StatutDossier.EN_VALIDATION_ADMIN));
        model.addAttribute("nbInscriptionsValidees",
                inscriptionService.countByStatut(StatutDossier.VALIDE));
        model.addAttribute("nbInscriptionsRejetees",
                inscriptionService.countByStatut(StatutDossier.REJETE));

        // ── Soutenances ───────────────────────────────────────────────────────
        model.addAttribute("nbSoutenancesSoumises",
                soutenanceService.countByStatut(StatutSoutenance.SOUMISE));   // countByStatut()
        model.addAttribute("nbSoutenancesRapports",
                soutenanceService.countByStatut(StatutSoutenance.RAPPORTS_RECUS));
        model.addAttribute("nbSoutenancesAutorisees",
                soutenanceService.countByStatut(StatutSoutenance.AUTORISEE));
        model.addAttribute("nbSoutenancesPlanifiees",
                soutenanceService.countByStatut(StatutSoutenance.PLANIFIEE));

        return "admin/dashboard";
    }

    @GetMapping("/doctorants")
    public String listeDoctorants(Model model) {

        model.addAttribute("doctorants", doctorantService.findAll()); // findAll()
        model.addAttribute("statuts",    StatutDoctorant.values());
        return "admin/doctorants";
    }

    @GetMapping("/doctorants/par-statut/{statut}")
    public String doctorantsParStatut(
            @PathVariable StatutDoctorant statut, Model model) {

        model.addAttribute("doctorants",  doctorantService.findByStatut(statut)); // findByStatut()
        model.addAttribute("statutActif", statut);
        model.addAttribute("statuts",     StatutDoctorant.values());
        return "admin/doctorants";
    }


    @GetMapping("/doctorants/directeur/{directeurId}")
    public String doctorantsParDirecteur(
            @PathVariable Long directeurId, Model model) {

        model.addAttribute("doctorants",
                doctorantService.findByDirecteurId(directeurId)); // findByDirecteurId()
        model.addAttribute("directeurId", directeurId);
        return "admin/doctorants";
    }

    @GetMapping("/doctorants/alertes")
    public String doctorantsEnAlerte(Model model) {

        model.addAttribute("doctorants",
                doctorantService.findDoctorantsEnAlerte()); // findDoctorantsEnAlerte()
        return "admin/alertes";
    }

    @GetMapping("/doctorants/{id}")
    public String ficheDoctorant(@PathVariable Long id, Model model) {

        Doctorant doc = doctorantService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable : " + id));

        model.addAttribute("doctorant",    doc);

        // Publications
        model.addAttribute("publications",
                publicationService.findByDoctorantId(id));
        model.addAttribute("articlesQ1Q2",
                publicationService.countArticlesQ1Q2Valides(id));
        model.addAttribute("conferences",
                publicationService.countConferencesValidees(id));

        // Formations
        model.addAttribute("formations",
                formationService.findByDoctorantId(id));
        model.addAttribute("totalHeures",
                formationService.getTotalHeuresValidees(id));
        // Soutenance
        model.addAttribute("soutenance",
                soutenanceService.findByDoctorantId(id).orElse(null));
        // Statut global
        model.addAttribute("prerequisOk",
                doctorantService.verifierPrerequis(id));
        model.addAttribute("peutReinscrire",
                doctorantService.peutSeReinscrire(id));

        //ça c'est  Pour le formulaire de changement de statut
        model.addAttribute("statuts", StatutDoctorant.values());

        return "admin/doctorant-detail";
    }

    @PostMapping("/doctorants/{id}/statut")
    public String changerStatut(
            @PathVariable Long id,
            @RequestParam StatutDoctorant statut,
            RedirectAttributes ra) {

        doctorantService.changerStatut(id, statut); // changerStatut()
        ra.addFlashAttribute("successMessage",
                "Statut mis à jour : " + statut.name() + ".");
        return "redirect:/admin/doctorants/" + id;
    }

    @PostMapping("/doctorants/{id}/supprimer")
    public String supprimerLogique(
            @PathVariable Long id,
            RedirectAttributes ra) {

        doctorantService.supprimerLogique(id); // supprimerLogique()
        ra.addFlashAttribute("warningMessage",
                "Compte désactivé. Le doctorant ne peut plus se connecter.");
        return "redirect:/admin/doctorants";
    }
}
