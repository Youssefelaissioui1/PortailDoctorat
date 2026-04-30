package com.devbuild.Portail_de_suivi_du_doctorat.Controller;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.FormationService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.PublicationService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/doctorant")
@PreAuthorize("hasRole('DOCTORANT')")
@RequiredArgsConstructor
public class Doctorantcontroller {

    private final DoctorantService   doctorantService;
    private final PublicationService publicationService;
    private final FormationService   formationService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);
        Long id = doc.getId();

        long articlesQ1Q2 = publicationService.countArticlesQ1Q2Valides(id);
        long conferences  = publicationService.countConferencesValidees(id);

        int heures = formationService.getTotalHeuresValidees(id);

        boolean prerequisOk      = doctorantService.verifierPrerequis(id);
        boolean peutReinscrire   = doctorantService.peutSeReinscrire(id);
        int     annees           = doc.getNombreAnnees();

        model.addAttribute("doctorant",         doc);
        model.addAttribute("articlesQ1Q2",       articlesQ1Q2);
        model.addAttribute("conferences",        conferences);
        model.addAttribute("heuresFormation",    heures);
        model.addAttribute("prerequisOk",        prerequisOk);
        model.addAttribute("peutReinscrire",     peutReinscrire);
        model.addAttribute("alerteAnneesDepasse", annees > 3);
        model.addAttribute("alerteLimite6Ans",   annees >= 6);

        return "doctorant/dashboard";
    }

    @GetMapping("/profil")
    public String profilPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);

        Doctorant docComplet = doctorantService.findById(doc.getId()) // findById()
                .orElseThrow(() -> new IllegalStateException("Doctorant introuvable"));

        model.addAttribute("doctorant", docComplet);
        return "doctorant/profil";
    }


    @PostMapping("/profil")
    public String updateProfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute Doctorant form,
            RedirectAttributes ra) {

        Doctorant doc = getDoctorant(userDetails);

        doc.setSujetThese(form.getSujetThese());
        doc.setLaboratoire(form.getLaboratoire());
        doc.setSpecialite(form.getSpecialite());
        doc.setNom(form.getNom());
        doc.setPrenom(form.getPrenom());

        doctorantService.modifier(doc);

        ra.addFlashAttribute("successMessage", "Profil mis à jour avec succès.");
        return "redirect:/doctorant/profil";
    }

    @GetMapping("/progression")
    public String progression(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);
        Long id = doc.getId();

        model.addAttribute("doctorant",    doc);
        model.addAttribute("publications", publicationService.findByDoctorantId(id)); // findByDoctorantId()
        model.addAttribute("formations",   formationService.findByDoctorantId(id));   // findByDoctorantId()
        model.addAttribute("articlesQ1Q2", publicationService.countArticlesQ1Q2Valides(id));
        model.addAttribute("conferences",  publicationService.countConferencesValidees(id));
        model.addAttribute("heures",       formationService.getTotalHeuresValidees(id));
        model.addAttribute("prerequisOk",  doctorantService.verifierPrerequis(id));

        model.addAttribute("seuilArticles",    2);
        model.addAttribute("seuilConferences", 2);
        model.addAttribute("seuilHeures",      200);

        return "doctorant/progression";
    }

    @GetMapping("/profil/{id}")
    @PreAuthorize("hasAnyRole('DOCTORANT','DIRECTEUR_THESE','PERSONNEL_ADMIN')")
    public String ficheParId(@PathVariable Long id, Model model) {

        Doctorant doc = doctorantService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable : " + id));

        model.addAttribute("doctorant",    doc);
        model.addAttribute("articlesQ1Q2", publicationService.countArticlesQ1Q2Valides(id));
        model.addAttribute("conferences",  publicationService.countConferencesValidees(id));
        model.addAttribute("heures",       formationService.getTotalHeuresValidees(id));
        model.addAttribute("prerequisOk",  doctorantService.verifierPrerequis(id));
        return "doctorant/fiche";
    }
    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('DIRECTEUR_THESE','PERSONNEL_ADMIN')")
    public String rechercheParCne(@RequestParam String cne, Model model) {

        model.addAttribute("resultat",
                doctorantService.findByCne(cne).orElse(null)); // findByCne()
        model.addAttribute("cneRecherche", cne);
        return "doctorant/recherche";
    }

    private Doctorant getDoctorant(UserDetails userDetails) {
        return doctorantService.findByEmail(userDetails.getUsername()) // findByEmail()
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable : " + userDetails.getUsername()));
    }
}