package com.devbuild.Portail_de_suivi_du_doctorat.Controller.API;

import com.devbuild.Portail_de_suivi_du_doctorat.Services.PublicationService;
import com.devbuild.Portail_de_suivi_du_doctorat.entities.Publication;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.StatutDossier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/publications")
@RequiredArgsConstructor
public class PublicationApiController {

    private final PublicationService publicationService;

    // Postman : GET http://localhost:8080/api/publications/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Publication> findById(@PathVariable Long id) {
        return publicationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Postman : GET http://localhost:8080/api/publications/doctorant/1
    @GetMapping("/doctorant/{doctorantId}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<List<Publication>> findByDoctorantId(@PathVariable Long doctorantId) {
        return ResponseEntity.ok(publicationService.findByDoctorantId(doctorantId));
    }

   // Postman : GET http://localhost:8080/api/publications/statut/EN_ATTENTE
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE')")
    public ResponseEntity<List<Publication>> findByStatut(@PathVariable StatutDossier statut) {
        return ResponseEntity.ok(publicationService.findByStatut(statut));
    }


    // Postman : GET http://localhost:8080/api/publications/doctorant/1/count-articles
    @GetMapping("/doctorant/{id}/count-articles")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Map<String, Object>> countArticlesQ1Q2(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "doctorantId",    id,
                "articlesQ1Q2",   publicationService.countArticlesQ1Q2Valides(id),
                "seuilRequis",    2
        ));
    }

    // Postman : GET http://localhost:8080/api/publications/doctorant/1/count-conferences
    @GetMapping("/doctorant/{id}/count-conferences")
    @PreAuthorize("hasAnyRole('PERSONNEL_ADMIN','DIRECTEUR_THESE','DOCTORANT')")
    public ResponseEntity<Map<String, Object>> countConferences(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "doctorantId",  id,
                "conferences",  publicationService.countConferencesValidees(id),
                "seuilRequis",  2
        ));
    }

   // Postman : POST http://localhost:8080/api/publications?doctorantId=1
    // Body (JSON) :
    // {
    //   "titre": "Deep Learning pour la détection...",
    //   "type": "ARTICLE_JOURNAL",
    //   "quartile": "Q1",
    //   "revueOuConference": "IEEE Transactions on AI",
    //   "doi": "10.1109/example",
    //   "datePublication": "2024-03-15"
    // }
    @PostMapping
    @PreAuthorize("hasRole('DOCTORANT')")
    public ResponseEntity<Publication> ajouter(
            @RequestParam Long doctorantId,
            @RequestBody Publication publication) {
        try {
            Publication saved = publicationService.ajouter(doctorantId, publication);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Postman : PATCH http://localhost:8080/api/publications/1/valider
    // Body (JSON) : { "commentaire": "Publication reconnue, journal indexé" }
    @PatchMapping("/{id}/valider")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Publication> valider(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(publicationService.valider(id, body.get("commentaire")));
    }

     // Postman : PATCH http://localhost:8080/api/publications/1/rejeter
    // Body (JSON) : { "motif": "Journal non reconnu" }
    @PatchMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('PERSONNEL_ADMIN')")
    public ResponseEntity<Publication> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(publicationService.rejeter(id, body.get("motif")));
    }
}