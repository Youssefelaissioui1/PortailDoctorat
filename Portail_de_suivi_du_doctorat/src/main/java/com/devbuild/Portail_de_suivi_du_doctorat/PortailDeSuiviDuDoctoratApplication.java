package com.devbuild.Portail_de_suivi_du_doctorat;

import com.devbuild.Portail_de_suivi_du_doctorat.repositories.DoctorantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class PortailDeSuiviDuDoctoratApplication implements CommandLineRunner {

    DoctorantRepository doctorantRepository;

    public PortailDeSuiviDuDoctoratApplication(
            DoctorantRepository doctorantRepository
    ) {
        this.doctorantRepository = doctorantRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(
                PortailDeSuiviDuDoctoratApplication.class,
                args
        );
    }

    @Override
    public void run(String... args) throws Exception {

    }

    @Bean
    CommandLineRunner testPassword(PasswordEncoder encoder) {

        return args -> {

            System.out.println(
                    encoder.encode("admin123")
            );

        };
    }
}