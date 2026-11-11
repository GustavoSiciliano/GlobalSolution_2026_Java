
package br.com.fiap.binova.api;

import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.StatusOcorrencia;
import br.com.fiap.binova.domain.Usuario;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.AutenticacaoService;
import br.com.fiap.binova.service.OcorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ocorrencias")
@RequiredArgsConstructor
@Tag(name = "Ocorrências", description = "Relatos registrados pelos cidadãos")
@SecurityRequirement(name = "bearerAuth")
public class OcorrenciasController {

    private final OcorrenciaService ocorrencias;
    private final AutenticacaoService autenticacao;

    // Lista as ocorrências conforme o usuário e o status.
    @Operation(summary = "Lista relatos conforme o papel e o status")
    @GetMapping
    public Page<EntityModel<Saidas.Ocorrencia>> listar(
            Authentication auth,
            @RequestParam(required = false) StatusOcorrencia status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pagina) {

        Usuario usuario = autenticacao.usuarioAutenticado(auth.getName());

        return ocorrencias.listar(auth.getName(), status, pagina)
                .map(item -> modelo(item, usuario));
    }

    // Consulta uma ocorrência permitida ao usuário.
    @Operation(summary = "Consulta o relato do autor ou administrador")
    @GetMapping("/{id}")
    public EntityModel<Saidas.Ocorrencia> detalhar(
            @PathVariable Long id, Authentication auth) {

        Saidas.Ocorrencia ocorrencia = ocorrencias.detalhar(id, auth.getName());
        Usuario usuario = autenticacao.usuarioAutenticado(auth.getName());

        return modelo(ocorrencia, usuario);
    }

    // Registra uma nova ocorrência com o usuário autenticado.
    @Operation(summary = "Registra uma nova ocorrência")
    @PostMapping
    public ResponseEntity<EntityModel<Saidas.Ocorrencia>> criar(
            @RequestBody @Valid Entradas.OcorrenciaNova dados,
            Authentication auth) {

        Saidas.Ocorrencia ocorrencia = ocorrencias.criar(dados, auth.getName());
        Usuario usuario = autenticacao.usuarioAutenticado(auth.getName());

        return ResponseEntity.created(URI.create("/api/ocorrencias/" + ocorrencia.id()))
                .body(modelo(ocorrencia, usuario));
    }

    // Atualiza o tipo e a descrição de uma ocorrência.
    @Operation(summary = "Altera o tipo e a descrição do relato")
    @PutMapping("/{id}")
    public EntityModel<Saidas.Ocorrencia> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid Entradas.OcorrenciaEdicao dados,
            Authentication auth) {

        Saidas.Ocorrencia ocorrencia = ocorrencias.atualizar(id, dados, auth.getName());
        Usuario usuario = autenticacao.usuarioAutenticado(auth.getName());

        return modelo(ocorrencia, usuario);
    }

    // Altera o status de uma ocorrência como administrador.
    @Operation(summary = "Atualiza o status (somente administrador)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<Saidas.Ocorrencia> mudarStatus(
            @PathVariable Long id,
            @RequestBody @Valid Entradas.Status dados,
            Authentication auth) {

        Saidas.Ocorrencia ocorrencia = ocorrencias.mudarStatus(id, dados.status());
        Usuario usuario = autenticacao.usuarioAutenticado(auth.getName());

        return modelo(ocorrencia, usuario);
    }

    // Exclui uma ocorrência conforme as regras de permissão.
    @Operation(summary = "Remove relato permitido pela regra de negócio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id, Authentication auth) {

        ocorrencias.excluir(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // Adiciona links HATEOAS conforme as permissões do usuário.
    private EntityModel<Saidas.Ocorrencia> modelo(
            Saidas.Ocorrencia ocorrencia, Usuario usuario) {

        Long id = ocorrencia.id();

        EntityModel<Saidas.Ocorrencia> resposta = EntityModel.of(ocorrencia,
                linkTo(methodOn(OcorrenciasController.class).detalhar(id, null)).withSelfRel(),
                linkTo(PontosColetaController.class).slash(ocorrencia.pontoColetaId()).withRel("ponto"));

        boolean administrador = usuario.getPapel() == Papel.ADMIN;
        boolean autor = usuario.getId().equals(ocorrencia.usuarioId());

        if (administrador) {
            resposta.add(linkTo(OcorrenciasController.class).slash(id).slash("status")
                    .withRel("alterar-status"));
        }

        if ((administrador || autor) && ocorrencia.status() != StatusOcorrencia.RESOLVIDA) {
            resposta.add(linkTo(OcorrenciasController.class).slash(id).withRel("editar"));
        }

        if (administrador || (autor && ocorrencia.status() == StatusOcorrencia.ABERTA)) {
            resposta.add(linkTo(OcorrenciasController.class).slash(id).withRel("excluir"));
        }

        return resposta;
    }
}
