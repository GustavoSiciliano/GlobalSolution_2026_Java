
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
@OpenAPIDefinition(info = @Info(
        title = "Binova API",
        version = "1.0.0",
        description = "API Java do Binova: pontos de coleta, leituras e ocorrencias"
))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class ApiConfig {

    // Configura o relógio utilizado nas leituras
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // Cria usuários de demonstração quando habilitado
    @Bean
    public CommandLineRunner usuariosDeDemonstracao(
            UsuarioRepository usuarios,
            PasswordEncoder codificador,
            @Value("${app.demo.enabled:false}") boolean demo,
            @Value("${app.demo.admin.password:}") String senhaAdmin,
            @Value("${app.demo.cidadao.password:}") String senhaCidadao) {

        return args -> {
            if (!demo) {
                return;
            }

            if (senhaAdmin.isBlank() || senhaCidadao.isBlank()) {
                throw new IllegalStateException("Configure as senhas dos usuarios de demonstracao");
            }

            adicionarUsuario(usuarios, codificador,
                    "Administrador", "admin@binova.local", senhaAdmin, Papel.ADMIN);

            adicionarUsuario(usuarios, codificador,
                    "Cidadao", "cidadao@binova.local", senhaCidadao, Papel.CIDADAO);
        };
    }

    // Cadastra o usuário caso ainda não exista
    private void adicionarUsuario(UsuarioRepository usuarios, PasswordEncoder codificador,
                                  String nome, String email, String senha, Papel papel) {

        if (usuarios.existsByEmailIgnoreCase(email)) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenhaHash(codificador.encode(senha));
        usuario.setPapel(papel);

        usuarios.save(usuario);
    }
}
