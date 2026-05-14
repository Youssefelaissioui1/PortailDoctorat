package com.devbuild.Portail_de_suivi_du_doctorat.Controller.thymleaf;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DoctorantService doctorantService;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {

        if (error  != null) model.addAttribute("errorMessage",  "Email ou mot de passe incorrect.");
        if (logout != null) model.addAttribute("logoutMessage", "Déconnexion réussie.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("doctorant", new Doctorant());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("doctorant") Doctorant doctorant,
            BindingResult result,
            RedirectAttributes ra) {

        if (result.hasErrors()) return "auth/register";

        try {
            doctorantService.creer(doctorant);   // ← creer()
            ra.addFlashAttribute("successMessage", "Compte créé ! Vous pouvez vous connecter.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.doctorant", e.getMessage());
            return "auth/register";
        }
    }
}