package com.devbuild.Portail_de_suivi_du_doctorat.Controller.thymleaf;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.PublicationService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Publication;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.QuartileEnum;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypePublication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class Publicationcontroller {

    private final PublicationService publicationService;
    private final DoctorantService   doctorantService;


    @GetMapping("/publications")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String mesPublications(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);
        Long id = doc.getId();

        model.addAttribute("publications",
                publicationService.findByDoctorantId(id));          // findByDoctorantId()
        model.addAttribute("articlesQ1Q2",
                publicationService.countArticlesQ1Q2Valides(id));   // countArticlesQ1Q2Valides()
        model.addAttribute("conferences",
                publicationService.countConferencesValidees(id));   // countConferencesValidees()

        // Seuils requis pour les barres de progression
        model.addAttribute("seuilArticles",    2);
        model.addAttribute("seuilConferences", 2);

        return "publication/liste";
    }

    @GetMapping("/publications/nouvelle")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String formulaire(Model model) {
        model.addAttribute("publication", new Publication());
        model.addAttribute("types",     TypePublication.values());
        model.addAttribute("quartiles", QuartileEnum.values());
        return "publication/formulaire";
    }

    @PostMapping("/publications")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String soumettre(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Publication publication,
            RedirectAttributes ra) {

        Doctorant doc = getDoctorant(userDetails);
        try {
            publicationService.ajouter(doc.getId(), publication); // ajouter()
            ra.addFlashAttribute("successMessage",
                    "Publication soumise et en attente de validation administrative.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/publications";
    }

    @GetMapping("/publications/{id}")
    @PreAuthorize("hasAnyRole('DOCTORANT','DIRECTEUR_THESE','PERSONNEL_ADMIN')")
    public String detail(@PathVariable Long id, Model model) {

        Publication pub = publicationService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable : " + id));

        model.addAttribute("publication", pub);
        return "publication/detail";
    }

    @GetMapping("/admin/publications")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String publicationsEnAttente(Model model) {

        model.addAttribute("publications",
                publicationService.findByStatut(StatutDossier.EN_ATTENTE)); // findByStatut()

        // Compteurs pour l'onglet d'administration
        model.addAttribute("nbEnAttente",
                publicationService.findByStatut(StatutDossier.EN_ATTENTE).size());
        model.addAttribute("nbValides",
                publicationService.findByStatut(StatutDossier.VALIDE).size());
        model.addAttribute("nbRejetes",
                publicationService.findByStatut(StatutDossier.REJETE).size());

        return "admin/publications";
    }

    @GetMapping("/admin/publications/par-statut/{statut}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String publicationsParStatut(
            @PathVariable StatutDossier statut, Model model) {

        model.addAttribute("publications",
                publicationService.findByStatut(statut)); // findByStatut()
        model.addAttribute("statutActif",  statut);
        model.addAttribute("tousStatuts",  StatutDossier.values());
        return "admin/publications";
    }

    @GetMapping("/admin/publications/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String detailAdmin(@PathVariable Long id, Model model) {

        model.addAttribute("publication",
                publicationService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Publication introuvable : " + id)));
        return "admin/publication-detail";
    }

    @PostMapping("/admin/publications/{id}/valider")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String valider(
            @PathVariable Long id,
            @RequestParam String commentaire,
            RedirectAttributes ra) {

        publicationService.valider(id, commentaire); // valider()
        ra.addFlashAttribute("successMessage", "Publication validée.");
        return "redirect:/admin/publications";
    }


    @PostMapping("/admin/publications/{id}/rejeter")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String rejeter(
            @PathVariable Long id,
            @RequestParam String motif,
            RedirectAttributes ra) {

        publicationService.rejeter(id, motif); // rejeter()
        ra.addFlashAttribute("warningMessage", "Publication rejetée.");
        return "redirect:/admin/publications";
    }

    private Doctorant getDoctorant(UserDetails userDetails) {
        return doctorantService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable : " + userDetails.getUsername()));
    }
}