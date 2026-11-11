package br.com.fiap.binova.config;

import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.Usuario;
import br.com.fiap.binova.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Binova API", version = "1.0.0",
    description = "API Java da Global Solution CitizenTech: pontos, leituras e ocorrencias"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class ApiConfig {

    // Configura o relógio para as leituras
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // Cria contas de demonstração na primeira execução
    @Bean
    public CommandLineRunner usuariosDeDemonstracao(UsuarioRepository usuarios,
            PasswordEncoder codificador, @Value("${app.demo.enabled:true}") boolean demo) {
        return args -> {
            if (!demo) return;
            adicionarUsuario(usuarios, codificador, "Administrador Binova", "admin@binova.local", "Admin@12345", Papel.ADMIN);
            adicionarUsuario(usuarios, codificador, "Cidadao Binova", "cidadao@binova.local", "Cidadao@12345", Papel.CIDADAO);
        };
    }

    // Evita duplicar usuarios quando o H2 persistente e reiniciado
    private void adicionarUsuario(UsuarioRepository usuarios, PasswordEncoder codificador, String nome, String email, String senha, Papel papel) {
        if (usuarios.existsByEmailIgnoreCase(email))
            return;
        Usuario usuario = new Usuario();

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(codificador.encode(senha));
        usuario.setPapel(papel);
        usuarios.save(usuario);
    }
}
