package com.devbuild.Portail_de_suivi_du_doctorat.repositories;


import com.devbuild.Portail_de_suivi_du_doctorat.entities.Utilisateur;

import com.devbuild.Portail_de_suivi_du_doctorat.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(RoleEnum role);

    List<Utilisateur> findByActif(boolean actif);
}