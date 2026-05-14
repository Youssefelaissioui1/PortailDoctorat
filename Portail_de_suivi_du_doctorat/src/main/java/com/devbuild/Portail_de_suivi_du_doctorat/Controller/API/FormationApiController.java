package com.devbuild.Portail_de_suivi_du_doctorat.Controller.API;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.FormationService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.FormationDoctorate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationApiController {

    private final FormationService formationService;

    // Postman : GET http://localhost:8080/api/formations/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DOCTORANT')")
    public ResponseEntity<FormationDoctorate> findById(@PathVariable Long id) {
        return formationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // Postman : GET http://localhost:8080/api/formations/doctorant/1
    @GetMapping("/doctorant/{doctorantId}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<List<FormationDoctorate>> findByDoctorantId(
            @PathVariable Long doctorantId) {
        return ResponseEntity.ok(formationService.findByDoctorantId(doctorantId));
    }

    // Postman : GET http://localhost:8080/api/formations/doctorant/1/heures
    @GetMapping("/doctorant/{doctorantId}/heures")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Map<String, Object>> getTotalHeures(@PathVariable Long doctorantId) {
        int total = formationService.getTotalHeuresValidees(doctorantId);
        return ResponseEntity.ok(Map.of(
                "doctorantId",   doctorantId,
                "totalHeures",   total,
                "seuilRequis",   200,
                "seuilAtteint",  total >= 200
        ));
    }

    // Postman : POST http://localhost:8080/api/formations?doctorantId=1
    // Body (JSON) :
    // {
    //   "intitule": "Machine Learning fondamental",
    //   "heures": 40,
    //   "dateFormation": "2024-11-10",
    //   "organisateur": "CNRST"
    // }
    @PostMapping
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<FormationDoctorate> ajouter(
            @RequestParam Long doctorantId,
            @RequestBody FormationDoctorate formation) {
        try {
            FormationDoctorate saved = formationService.ajouter(doctorantId, formation);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : PATCH http://localhost:8080/api/formations/1/valider
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<FormationDoctorate> valider(@PathVariable Long id) {
        try {
            FormationDoctorate f = formationService.valider(id);
            return ResponseEntity.ok(f);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}