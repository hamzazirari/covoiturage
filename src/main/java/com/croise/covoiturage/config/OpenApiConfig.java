package com.croise.covoiturage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI covoiturageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Plateforme de Covoiturage Multi-Étapes")
                        .description("""
                                API REST permettant aux conducteurs de publier des trajets
                                avec plusieurs points d'arrêt, et aux passagers de rechercher
                                et réserver des places sur tout ou partie du trajet.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ton Nom")
                                .email("ton.email@exemple.com")));
    }
}