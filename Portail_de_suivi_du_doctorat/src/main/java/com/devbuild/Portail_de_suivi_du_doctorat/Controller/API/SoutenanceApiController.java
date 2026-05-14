package com.devbuild.Portail_de_suivi_du_doctorat.Controller.API;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.SoutenanceService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.MembreJury;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Soutenance;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutSoutenance;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soutenances")
@RequiredArgsConstructor
public class SoutenanceApiController {

    private final SoutenanceService soutenanceService;

    // Postman : GET http://localhost:8080/api/soutenances/en-cours
    @GetMapping("/en-cours")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<List<Soutenance>> findEnCours() {
        return ResponseEntity.ok(soutenanceService.findEnCours());
    }

   // Postman : GET http://localhost:8080/api/soutenances/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Soutenance> findById(@PathVariable Long id) {
        return soutenanceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/soutenances/doctorant/1
    @GetMapping("/doctorant/{doctorantId}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Soutenance> findByDoctorantId(@PathVariable Long doctorantId) {
        return soutenanceService.findByDoctorantId(doctorantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/soutenances/statut/SOUMISE
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<List<Soutenance>> findByStatut(@PathVariable StatutSoutenance statut) {
        return ResponseEntity.ok(soutenanceService.findByStatut(statut));
    }

    // Postman : GET http://localhost:8080/api/soutenances/count/SOUMISE
    @GetMapping("/count/{statut}")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Map<String, Object>> countByStatut(@PathVariable StatutSoutenance statut) {
        return ResponseEntity.ok(Map.of(
                "statut", statut.name(),
                "count",  soutenanceService.countByStatut(statut)
        ));
    }

    // Postman : POST http://localhost:8080/api/soutenances/creer?doctorantId=1
    @PostMapping("/creer")
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<Soutenance> creerDemande(@RequestParam Long doctorantId) {
        try {
            Soutenance soutenance = soutenanceService.creerDemande(doctorantId);
            return ResponseEntity.status(HttpStatus.CREATED).body(soutenance);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : PATCH http://localhost:8080/api/soutenances/1/soumettre
    @PatchMapping("/{id}/soumettre")
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<Soutenance> soumettre(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(soutenanceService.soumettre(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : POST http://localhost:8080/api/soutenances/1/jury
    // Body (JSON) :
    // {
    //   "nom": "Benali",
    //   "prenom": "Hassan",
    //   "email": "hbenali@univ.ma",
    //   "grade": "Professeur",
    //   "etablissement": "Université Mohammed V",
    //   "qualite": "RAPPORTEUR"
    // }
    @PostMapping("/{id}/jury")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public ResponseEntity<MembreJury> ajouterMembreJury(
            @PathVariable Long id,
            @RequestBody MembreJury membre) {
        try {
            MembreJury saved = soutenanceService.ajouterMembreJury(id, membre);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : PATCH http://localhost:8080/api/soutenances/rapport/1?favorable=true
    @PatchMapping("/rapport/{membreJuryId}")
    @PreAuthorize("hasRole('DIRECTEUR_THESE')")
    public ResponseEntity<Map<String, String>> soumettreRapport(
            @PathVariable Long membreJuryId,
            @RequestParam boolean favorable) {
        soutenanceService.soumettreRapportJury(membreJuryId, favorable);
        return ResponseEntity.ok(Map.of(
                "message",   "Rapport soumis.",
                "favorable", String.valueOf(favorable)
        ));
    }

      // Postman : PATCH http://localhost:8080/api/soutenances/1/autoriser
    // Body (JSON) : { "commentaire": "Rapports favorables reçus" }
    @PatchMapping("/{id}/autoriser")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Soutenance> autoriser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(soutenanceService.autoriser(id, body.get("commentaire")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

     // Postman : PATCH http://localhost:8080/api/soutenances/1/planifier
    // Params query :
    //   ?date=2025-06-15&heure=10:00&lieu=Salle A, Bâtiment Faculté
    @PatchMapping("/{id}/planifier")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Soutenance> planifier(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime heure,
            @RequestParam String lieu) {
        try {
            return ResponseEntity.ok(soutenanceService.planifier(id, date, heure, lieu));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}