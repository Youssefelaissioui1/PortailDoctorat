package com.devbuild.Portail_de_suivi_du_doctorat;

import com.devbuild.Portail_de_suivi_du_doctorat.entities.Doctorant;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import com.devbuild.Portail_de_suivi_du_doctorat.repositories.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortailDeSuiviDuDoctoratApplication implements CommandLineRunner {


    public PortailDeSuiviDuDoctoratApplication(DoctorantRepository doctorantRepository) {
        this.doctorantRepository = doctorantRepository;
    }

    //    @Autowired
DoctorantRepository doctorantRepository;
	public static void main(String[] args) {
		SpringApplication.run(PortailDeSuiviDuDoctoratApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
    }
}
