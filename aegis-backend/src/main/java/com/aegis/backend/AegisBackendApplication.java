package com.aegis.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.net.URI;

@SpringBootApplication
public class AegisBackendApplication {

    public static void main(String[] args) {
        configurarBancoParaProducao();
        SpringApplication.run(AegisBackendApplication.class, args);
    }

    private static void configurarBancoParaProducao() {
        try {
            String databaseUrl = System.getenv("DATABASE_URL");
            if (databaseUrl != null && !databaseUrl.isBlank()) {
                URI dbUri = new URI(databaseUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

                System.setProperty("spring.datasource.url", dbUrl);
                System.setProperty("spring.datasource.username", username);
                System.setProperty("spring.datasource.password", password);
                
                System.out.println(">>> Configuração de Banco Fly.io Detectada e Aplicada!");
            }
        } catch (Exception e) {
            System.err.println(">>> AVISO: Não foi possível configurar banco via URL: " + e.getMessage());
        }
    }
}