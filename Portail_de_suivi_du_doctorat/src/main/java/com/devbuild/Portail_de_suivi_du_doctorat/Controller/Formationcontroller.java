package com.devbuild.Portail_de_suivi_du_doctorat.Controller;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.Services.FormationService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.FormationDoctorate;
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
public class Formationcontroller {

    private final FormationService formationService;
    private final DoctorantService doctorantService;

    @GetMapping("/formations")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String mesFormations(
            @AuthenticationPrincipal UserDetails userDetails, Model model) {

        Doctorant doc = getDoctorant(userDetails);
        Long id = doc.getId();

        model.addAttribute("formations",
                formationService.findByDoctorantId(id));        // findByDoctorantId()
        model.addAttribute("totalHeures",
                formationService.getTotalHeuresValidees(id));   // getTotalHeuresValidees()
        model.addAttribute("seuilHeures", 200);
        model.addAttribute("seuilAtteint",
                formationService.getTotalHeuresValidees(id) >= 200);

        return "formation/liste";
    }

    @GetMapping("/formations/nouvelle")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String formulaire(Model model) {
        model.addAttribute("formation", new FormationDoctorate());
        return "formation/formulaire";
    }

    @PostMapping("/formations")
    @PreAuthorize("hasRole('DOCTORANT')")
    public String soumettre(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute FormationDoctorate formation,
            RedirectAttributes ra) {

        Doctorant doc = getDoctorant(userDetails);
        try {
            formationService.ajouter(doc.getId(), formation); // ajouter()
            ra.addFlashAttribute("successMessage",
                    "Formation soumise et en attente de validation administrative.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/formations";
    }

    @GetMapping("/formations/{id}")
    @PreAuthorize("hasAnyRole('DOCTORANT','PERSONNEL_ADMIN')")
    public String detail(@PathVariable Long id, Model model) {

        FormationDoctorate formation = formationService.findById(id) // findById()
                .orElseThrow(() -> new IllegalArgumentException("Formation introuvable : " + id));

        model.addAttribute("formation", formation);
        return "formation/detail";
    }

    @GetMapping("/admin/formations")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String formationsAdmin(Model model) {

        model.addAttribute("formations", formationService.findNonValidees());

        return "admin/formations";
    }
    @GetMapping("/admin/formations/doctorant/{doctorantId}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String formationsParDoctorant(@PathVariable Long doctorantId, Model model) {

        Doctorant doc = doctorantService.findById(doctorantId)
                .orElseThrow(() -> new IllegalArgumentException("Doctorant introuvable"));

        model.addAttribute("doctorant",  doc);
        model.addAttribute("formations",
                formationService.findByDoctorantId(doctorantId)); // findByDoctorantId()
        model.addAttribute("totalHeures",
                formationService.getTotalHeuresValidees(doctorantId)); // getTotalHeuresValidees()
        model.addAttribute("seuilAtteint",
                formationService.getTotalHeuresValidees(doctorantId) >= 200);

        return "admin/formations";
    }

    @GetMapping("/admin/formations/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String detailAdmin(@PathVariable Long id, Model model) {

        model.addAttribute("formation",
                formationService.findById(id) // findById()
                        .orElseThrow(() -> new IllegalArgumentException("Formation introuvable : " + id)));
        return "admin/formation-detail";
    }

    @PostMapping("/admin/formations/{id}/valider")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public String valider(
            @PathVariable Long id,
            RedirectAttributes ra) {

        try {
            FormationDoctorate f = formationService.valider(id); // valider()
            ra.addFlashAttribute("successMessage",
                    "Formation \"" + f.getIntitule() + "\" validée (" + f.getHeures() + " h).");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/formations";
    }

    private Doctorant getDoctorant(UserDetails userDetails) {
        return doctorantService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable : " + userDetails.getUsername()));
    }
}
