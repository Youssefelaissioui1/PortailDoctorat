package com.devbuild.Portail_de_suivi_du_doctorat.repositories;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Document;
import com.devbuild.Portail_de_suivi_du_doctorat.enums.TypeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByInscriptionId(Long inscriptionId);

    List<Document> findBySoutenanceId(Long soutenanceId);

    List<Document> findByType(TypeDocument type);

    boolean existsByInscriptionIdAndType(Long inscriptionId, TypeDocument type);
}