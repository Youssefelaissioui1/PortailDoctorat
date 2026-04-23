package com.devbuild.Portail_de_suivi_du_doctorat.repositories;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.DirecteurThese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirecteurTheseRepository extends JpaRepository<DirecteurThese, Long> {

    Optional<DirecteurThese> findByEmail(String email);

    List<DirecteurThese> findBySpecialite(String specialite);

    List<DirecteurThese> findByEtablissement(String etablissement);
}