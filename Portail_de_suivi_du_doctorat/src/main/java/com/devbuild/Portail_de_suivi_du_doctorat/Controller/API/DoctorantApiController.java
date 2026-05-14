package com.devbuild.Portail_de_suivi_du_doctorat.Controller.API;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.DoctorantService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDoctorant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctorants")
@RequiredArgsConstructor
public class DoctorantApiController {

    private final DoctorantService doctorantService;

    // Postman : GET http://localhost:8080/api/doctorants
    @GetMapping
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<List<Doctorant>> findAll() {
        return ResponseEntity.ok(doctorantService.findAll());
    }

    // Postman : GET http://localhost:8080/api/doctorants/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Doctorant> findById(@PathVariable Long id) {
        return doctorantService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/doctorants/cne/CNE123
    @GetMapping("/cne/{cne}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<Doctorant> findByCne(@PathVariable String cne) {
        return doctorantService.findByCne(cne)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/doctorants/email/test@email.com
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<Doctorant> findByEmail(@PathVariable String email) {
        return doctorantService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/doctorants/statut/ACTIF
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<List<Doctorant>> findByStatut(@PathVariable StatutDoctorant statut) {
        return ResponseEntity.ok(doctorantService.findByStatut(statut));
    }

    // Postman : GET http://localhost:8080/api/doctorants/directeur/2
    @GetMapping("/directeur/{directeurId}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<List<Doctorant>> findByDirecteurId(@PathVariable Long directeurId) {
        return ResponseEntity.ok(doctorantService.findByDirecteurId(directeurId));
    }
    // Postman : GET http://localhost:8080/api/doctorants/alertes
    @GetMapping("/alertes")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<List<Doctorant>> findDoctorantsEnAlerte() {
        return ResponseEntity.ok(doctorantService.findDoctorantsEnAlerte());
    }

    // Postman : GET http://localhost:8080/api/doctorants/1/prerequis
    @GetMapping("/{id}/prerequis")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Map<String, Object>> verifierPrerequis(@PathVariable Long id) {
        boolean ok = doctorantService.verifierPrerequis(id);
        return ResponseEntity.ok(Map.of(
                "doctorantId",   id,
                "prerequisOk",   ok,
                "message",       ok
                        ? "Tous les prérequis sont remplis"
                        : "Prérequis manquants (2 articles Q1/Q2, 2 conférences, 200h formation)"
        ));
    }
    // Postman : GET http://localhost:8080/api/doctorants/1/peut-reinscrire
    @GetMapping("/{id}/peut-reinscrire")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Map<String, Object>> peutSeReinscrire(@PathVariable Long id) {
        boolean peut = doctorantService.peutSeReinscrire(id);
        return ResponseEntity.ok(Map.of(
                "doctorantId",     id,
                "peutReinscrire",  peut,
                "message",         peut
                        ? "Réinscription autorisée"
                        : "Durée de 3 ans dépassée — dérogation PED requise"
        ));
    }

    // Postman : POST http://localhost:8080/api/doctorants
    // Body (JSON) :
    // {
    //   "nom": "Alami",
    //   "prenom": "Youssef",
    //   "email": "youssef@test.com",
    //   "motDePasse": "password123",
    //   "cne": "CNE001",
    //   "sujetThese": "Intelligence artificielle",
    //   "datePremiereInscription": "2023-09-01"
    // }
    @PostMapping
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Doctorant> creer(@RequestBody Doctorant doctorant) {
        try {
            Doctorant saved = doctorantService.creer(doctorant);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // Postman : PUT http://localhost:8080/api/doctorants/1
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DOCTORANT')")
    public ResponseEntity<Doctorant> modifier(
            @PathVariable Long id,
            @RequestBody Doctorant doctorant) {
        try {
            doctorant.setId(id);
            Doctorant updated = doctorantService.modifier(doctorant);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


    // Postman : PATCH http://localhost:8080/api/doctorants/1/statut
    // Body (JSON) : { "statut": "DEROGATION_ACCORDEE" }
    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Map<String, String>> changerStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            StatutDoctorant statut = StatutDoctorant.valueOf(body.get("statut"));
            doctorantService.changerStatut(id, statut);
            return ResponseEntity.ok(Map.of(
                    "message", "Statut mis à jour : " + statut.name()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : DELETE http://localhost:8080/api/doctorants/1
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Map<String, String>> supprimerLogique(@PathVariable Long id) {
        try {
            doctorantService.supprimerLogique(id);
            return ResponseEntity.ok(Map.of(
                    "message", "Compte désactivé. Le doctorant ne peut plus se connecter."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}