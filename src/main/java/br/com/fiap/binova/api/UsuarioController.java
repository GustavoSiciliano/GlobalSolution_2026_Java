package br.com.fiap.binova.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.AutenticacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class UsuarioController {
    private final AutenticacaoService autenticacao;

    // Exibe os dados publicos do proprio usuario autenticado.
    @Operation(summary = "Retorna dados do usuário autenticado")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public Saidas.Usuario meuPerfil(Authentication auth) {
        return Saidas.Usuario.de(autenticacao.usuarioAutenticado(auth.getName()));
    }
}
