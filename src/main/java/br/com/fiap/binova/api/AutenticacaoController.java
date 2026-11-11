package br.com.fiap.binova.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.AutenticacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AutenticacaoController {
    private final AutenticacaoService servico;

    // Cria uma conta de cidadao com senha protegida
    @Operation(summary = "Cadastra um novo cidadão")
    @PostMapping("/cadastro")
    public ResponseEntity<Saidas.Usuario> cadastro(@RequestBody @Valid Entradas.Cadastro dados) {
        Saidas.Usuario usuario = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/usuarios/me")).body(usuario);
    }

    // Autentica e devolve o token Bearer
    @Operation(summary = "Confere email e senha e devolve JWT")
    @PostMapping("/login")
    public Saidas.Token login(@RequestBody @Valid Entradas.Login dados) {
        return servico.login(dados);
    }
}
