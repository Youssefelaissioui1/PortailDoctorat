package com.devbuild.Portail_de_suivi_du_doctorat.Controller.thymleaf;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.CampagneInscriptionService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.InscriptionService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.CampagneInscription;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Inscription;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class Inscriptioncontroller {

    private final InscriptionService         inscriptionService;
    private final CampagneInscriptionService campagneService;
    private final DoctorantService           doctorantService;
    @GetMapping("/inscriptions")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String mesInscriptions(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);
        model.addAttribute("inscriptions",
                inscriptionService.findByDoctorantId(doc.getId())); // findByDoctorantId()
        return "inscription/liste";
    }

    @GetMapping("/inscriptions/nouvelle")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String formulaireNouvelle(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);

        if (!doctorantService.peutSeReinscrire(doc.getId())) { // peutSeReinscrire()
            model.addAttribute("message",
                    "Votre inscription initiale date de plus de 3 ans. "
                            + "Une dérogation du PED est obligatoire.");
            return "inscription/bloquee";
        }

        Optional<CampagneInscription> campagneOpt =
                campagneService.findCampagneActive(); // findCampagneActive()

        if (campagneOpt.isEmpty()) {
            model.addAttribute("message",
                    "Aucune campagne d'inscription n'est actuellement ouverte.");
            return "inscription/bloquee";
        }

        model.addAttribute("inscription", new Inscription());
        model.addAttribute("campagne",    campagneOpt.get());
        model.addAttribute("doctorant",   doc);
        return "inscription/formulaire";
    }

    @PostMapping("/inscriptions")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String soumettre(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Inscription inscription,
            @RequestParam Long campagneId,
            RedirectAttributes ra) {

        Doctorant doc = getDoctorant(userDetails);
        try {
            Inscription saved = inscriptionService.soumettre(  // soumettre()
                    doc.getId(), campagneId, inscription);
            ra.addFlashAttribute("successMessage",
                    "Dossier soumis ! Votre directeur de thèse va recevoir une notification.");
            return "redirect:/inscriptions/" + saved.getId();
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/inscriptions/nouvelle";
        }
    }

    @GetMapping("/inscriptions/{id}")
    @PreAuthorize("hasAnyRole('DOCTORANT','DIRECTEUR_THESE','PERSONNEL_ADMIN')")
    public String detailInscription(@PathVariable Long id, Model model) {

        model.addAttribute("inscription",
                inscriptionService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable : " + id)));
        return "inscription/detail";
    }

    @GetMapping("/directeur/inscriptions")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String inscriptionsAValiderDirecteur(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        // On récupère l'entité du directeur via son email
        Doctorant directeurEntity = getDoctorant(userDetails);

        model.addAttribute("inscriptions",
                inscriptionService.findEnAttenteAvisDirecteur(directeurEntity.getId())); // findEnAttenteAvisDirecteur()
        return "directeur/inscriptions";
    }


    @GetMapping("/directeur/inscriptions/{id}")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String detailInscriptionDirecteur(@PathVariable Long id, Model model) {

        model.addAttribute("inscription",
                inscriptionService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable : " + id)));
        return "directeur/inscription-detail";
    }

    @PostMapping("/directeur/inscriptions/{id}/avis")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public String donnerAvis(
            @PathVariable Long id,
            @RequestParam String avis,
            RedirectAttributes ra) {

        try {
            inscriptionService.validerParDirecteur(id, avis); // validerParDirecteur()
            ra.addFlashAttribute("successMessage",
                    "Avis enregistré. Le dossier est transmis à l'administration.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/directeur/inscriptions";
    }

    @GetMapping("/admin/inscriptions")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String inscriptionsEnAttenteAdmin(Model model) {

        model.addAttribute("inscriptions",
                inscriptionService.findEnAttenteValidationAdmin()); // findEnAttenteValidationAdmin()

        // Compteurs par statut pour le tableau de bord
        model.addAttribute("nbEnAttenteDirecteur",
                inscriptionService.countByStatut(StatutDossier.EN_VALIDATION_DIRECTEUR)); // countByStatut()
        model.addAttribute("nbEnAttenteAdmin",
                inscriptionService.countByStatut(StatutDossier.EN_VALIDATION_ADMIN));
        model.addAttribute("nbValides",
                inscriptionService.countByStatut(StatutDossier.VALIDE));
        model.addAttribute("nbRejetes",
                inscriptionService.countByStatut(StatutDossier.REJETE));

        return "admin/inscriptions";
    }

    @GetMapping("/admin/inscriptions/toutes")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String toutesInscriptions(Model model) {

        // Utilise findByStatut() pour chaque statut — couvre toute la méthode
        java.util.List<Inscription> toutes = new java.util.ArrayList<>();
        for (StatutDossier s : StatutDossier.values()) {
            toutes.addAll(inscriptionService.findByStatut(s)); // findByStatut()
        }
        model.addAttribute("inscriptions", toutes);
        model.addAttribute("statutsFiltres", StatutDossier.values());
        return "admin/inscriptions-toutes";
    }

    @GetMapping("/admin/inscriptions/par-statut/{statut}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String inscriptionsParStatut(
            @PathVariable StatutDossier statut, Model model) {

        model.addAttribute("inscriptions",
                inscriptionService.findByStatut(statut)); // findByStatut()
        model.addAttribute("statutActif", statut);
        return "admin/inscriptions";
    }

    @GetMapping("/admin/inscriptions/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String detailInscriptionAdmin(@PathVariable Long id, Model model) {

        model.addAttribute("inscription",
                inscriptionService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Inscription introuvable : " + id)));
        return "admin/inscription-detail";
    }


    @PostMapping("/admin/inscriptions/{id}/valider")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String validerAdmin(
            @PathVariable Long id,
            @RequestParam String commentaire,
            RedirectAttributes ra) {

        try {
            inscriptionService.validerParAdmin(id, commentaire); // validerParAdmin()
            ra.addFlashAttribute("successMessage", "Dossier validé avec succès.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/inscriptions";
    }

    @PostMapping("/admin/inscriptions/{id}/rejeter")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String rejeterAdmin(
            @PathVariable Long id,
            @RequestParam String motif,
            RedirectAttributes ra) {

        inscriptionService.rejeter(id, motif); // rejeter()
        ra.addFlashAttribute("warningMessage", "Dossier rejeté.");
        return "redirect:/admin/inscriptions";
    }

    @GetMapping("/admin/campagnes")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String listeCampagnes(Model model) {

        model.addAttribute("campagnes",        campagneService.findAll());           // findAll()
        model.addAttribute("campagneActive",   campagneService.findCampagneActive()  // findCampagneActive()
                .orElse(null));
        model.addAttribute("nouvelleCampagne", new CampagneInscription());
        return "admin/campagnes";
    }

    @GetMapping("/admin/campagnes/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String detailCampagne(@PathVariable Long id, Model model) {

        model.addAttribute("campagne",
                campagneService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable : " + id)));
        return "admin/campagne-detail";
    }

    @PostMapping("/admin/campagnes")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String creerCampagne(
            @ModelAttribute CampagneInscription campagne,
            RedirectAttributes ra) {

        try {
            campagneService.creer(campagne); // creer()
            ra.addFlashAttribute("successMessage",
                    "Campagne " + campagne.getAnneeUniversitaire() + " créée.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/campagnes";
    }

    @PostMapping("/admin/campagnes/{id}/modifier")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String modifierCampagne(
            @PathVariable Long id,
            @ModelAttribute CampagneInscription form,
            RedirectAttributes ra) {

        CampagneInscription campagne = campagneService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Campagne introuvable"));

        campagne.setDateOuverture(form.getDateOuverture());
        campagne.setDateFermeture(form.getDateFermeture());
        campagne.setDescription(form.getDescription());

        campagneService.modifier(campagne); // modifier()
        ra.addFlashAttribute("successMessage", "Campagne modifiée.");
        return "redirect:/admin/campagnes/" + id;
    }

    @PostMapping("/admin/campagnes/{id}/activer")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String activerCampagne(@PathVariable Long id, RedirectAttributes ra) {

        campagneService.activer(id); // activer()
        ra.addFlashAttribute("successMessage", "Campagne activée. Les inscriptions sont ouvertes.");
        return "redirect:/admin/campagnes";
    }

    @PostMapping("/admin/campagnes/{id}/desactiver")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String desactiverCampagne(@PathVariable Long id, RedirectAttributes ra) {

        campagneService.desactiver(id); // desactiver()
        ra.addFlashAttribute("warningMessage", "Campagne désactivée. Plus d'inscriptions possibles.");
        return "redirect:/admin/campagnes";
    }
private Doctorant getDoctorant(UserDetails userDetails) {
        return doctorantService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable : " + userDetails.getUsername()));
    }
}
