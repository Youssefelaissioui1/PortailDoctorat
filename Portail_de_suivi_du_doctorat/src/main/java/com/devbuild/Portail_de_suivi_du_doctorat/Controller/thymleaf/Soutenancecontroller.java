package com.devbuild.Portail_de_suivi_du_doctorat.Controller.thymleaf;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.SoutenanceService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.MembreJury;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Soutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.QualiteJury;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequiredArgsConstructor
public class Soutenancecontroller {

    private final SoutenanceService soutenanceService;
    private final DoctorantService  doctorantService;

    @GetMapping("/soutenance")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String maSoutenance(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);

        model.addAttribute("doctorant",   doc);
        model.addAttribute("soutenance",
                soutenanceService.findByDoctorantId(doc.getId()) // findByDoctorantId()
                        .orElse(null));
        model.addAttribute("prerequisOk",
                doctorantService.verifierPrerequis(doc.getId()));            // verifierPrerequis()

        return "soutenance/ma-soutenance";
    }

    @PostMapping("/soutenance/creer")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String creerDemande(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes ra) {

        Doctorant doc = getDoctorant(userDetails);
        try {
            soutenanceService.creerDemande(doc.getId()); // creerDemande()
            ra.addFlashAttribute("successMessage",
                    "Demande créée en brouillon. Complétez votre dossier puis soumettez-le.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/soutenance";
    }

    @PostMapping("/soutenance/{id}/soumettre")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String soumettreDemande(
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {
            soutenanceService.soumettre(id); // soumettre()
            ra.addFlashAttribute("successMessage",
                    "Demande soumise. L'administration va examiner votre dossier.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/soutenance";
    }


    @GetMapping("/directeur/soutenances")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String listeSoutenancesDirecteur(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        model.addAttribute("soutenancesSoumises",
                soutenanceService.findByStatut(StatutSoutenance.SOUMISE));    // findByStatut()
        model.addAttribute("soutenancesAutorisees",
                soutenanceService.findByStatut(StatutSoutenance.AUTORISEE));  // findByStatut()
        return "directeur/soutenances";
    }

    @GetMapping("/directeur/soutenances/{id}")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String detailSoutenanceDirecteur(@PathVariable Long id, Model model) {

        Soutenance soutenance = soutenanceService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Soutenance introuvable : " + id));

        model.addAttribute("soutenance",   soutenance);
        model.addAttribute("membreJury",   new MembreJury());
        model.addAttribute("qualites",     QualiteJury.values());
        return "directeur/soutenance-detail";
    }

    @PostMapping("/directeur/soutenances/{id}/jury")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String ajouterMembreJury(
            @PathVariable Long id,
            @ModelAttribute MembreJury membre,
            RedirectAttributes ra) {

        try {
            MembreJury saved = soutenanceService.ajouterMembreJury(id, membre); // ajouterMembreJury()
            ra.addFlashAttribute("successMessage",
                    saved.getNomComplet() + " (" + saved.getQualite() + ") ajouté au jury.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/directeur/soutenances/" + id;
    }

    @PostMapping("/directeur/rapport/{membreId}")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String soumettreRapport(
            @PathVariable Long membreId,
            @RequestParam boolean favorable,
            RedirectAttributes ra) {

        soutenanceService.soumettreRapportJury(membreId, favorable); // soumettreRapportJury()
        ra.addFlashAttribute("successMessage",
                "Rapport soumis. " + (favorable ? "Avis favorable." : "Avis défavorable."));
        return "redirect:/directeur/soutenances";
    }

    @GetMapping("/admin/soutenances")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String listeSoutenancesAdmin(Model model) {

        model.addAttribute("soutenances",
                soutenanceService.findEnCours()); // findEnCours()
        model.addAttribute("nbBrouillon",
                soutenanceService.countByStatut(StatutSoutenance.BROUILLON));    // countByStatut()
        model.addAttribute("nbSoumises",
                soutenanceService.countByStatut(StatutSoutenance.SOUMISE));
        model.addAttribute("nbRapportsRecus",
                soutenanceService.countByStatut(StatutSoutenance.RAPPORTS_RECUS));
        model.addAttribute("nbAutorisees",
                soutenanceService.countByStatut(StatutSoutenance.AUTORISEE));
        model.addAttribute("nbPlanifiees",
                soutenanceService.countByStatut(StatutSoutenance.PLANIFIEE));

        return "admin/soutenances";
    }
    @GetMapping("/admin/soutenances/par-statut/{statut}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String soutenancesParStatut(
            @PathVariable StatutSoutenance statut, Model model) {

        model.addAttribute("soutenances",
                soutenanceService.findByStatut(statut)); // findByStatut()
        model.addAttribute("statutActif", statut);
        model.addAttribute("tousStatuts", StatutSoutenance.values());
        return "admin/soutenances";
    }

    @GetMapping("/admin/soutenances/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String detailSoutenanceAdmin(@PathVariable Long id, Model model) {

        Soutenance soutenance = soutenanceService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Soutenance introuvable : " + id));

        model.addAttribute("soutenance", soutenance);
        return "admin/soutenance-detail";
    }
    @PostMapping("/admin/soutenances/{id}/autoriser")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String autoriser(
            @PathVariable Long id,
            @RequestParam String commentaire,
            RedirectAttributes ra) {

        try {
            soutenanceService.autoriser(id, commentaire); // autoriser()
            ra.addFlashAttribute("successMessage",
                    "Soutenance autorisée. Vous pouvez maintenant la planifier.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/soutenances";
    }


    @PostMapping("/admin/soutenances/{id}/planifier")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String planifier(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
            @RequestParam String lieu,
            RedirectAttributes ra) {

        try {
            soutenanceService.planifier(id, date, heure, lieu); // planifier()
            ra.addFlashAttribute("successMessage",
                    "Soutenance planifiée le " + date + " à " + heure + " — " + lieu + ".");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/soutenances/" + id;
    }


    private Doctorant getDoctorant(UserDetails userDetails) {
        return doctorantService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable : " + userDetails.getUsername()));
    }
}