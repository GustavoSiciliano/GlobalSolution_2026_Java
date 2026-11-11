package br.com.fiap.binova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableCaching
public class BinovaApplication {

    // Cria o diretorio do H2
    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("data"));
        SpringApplication.run(BinovaApplication.class, args);
    }
}
