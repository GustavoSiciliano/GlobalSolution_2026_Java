
package br.com.fiap.binova.api;

import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação")
public class AutenticacaoController {

    private final AutenticacaoService servico;

    // Cadastra um cidadão com senha protegida
    @Operation(summary = "Cadastra um novo cidadão")
    @PostMapping("/cadastro")
    public ResponseEntity<Saidas.Usuario> cadastro(@RequestBody @Valid Entradas.Cadastro dados) {
        Saidas.Usuario usuario = servico.cadastrar(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    // Autentica o usuário e retorna um token
    @Operation(summary = "Confere email e senha e devolve JWT")
    @PostMapping("/login")
    public Saidas.Token login(@RequestBody @Valid Entradas.Login dados) {
        return servico.login(dados);
    }
}
