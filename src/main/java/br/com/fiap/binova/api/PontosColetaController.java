package br.com.fiap.binova.api;

import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.PontoColetaService;
import br.com.fiap.binova.service.SituacaoPontoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pontos-coleta")
@RequiredArgsConstructor
@Tag(name = "Pontos de coleta", description = "Cadastro, busca e situação das lixeiras")
public class PontosColetaController {
    private final PontoColetaService pontos;
    private final SituacaoPontoService situacao;

    // Pagina pontos e aplica filtros dinamicos (Specification).
    @Operation(summary = "Lista pontos com filtros e paginação")
    @GetMapping
    public Page<EntityModel<Saidas.Ponto>> listar(@RequestParam(required = false) String termo,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 10, sort = "id") Pageable pagina) {
        return pontos.listar(termo, ativo, pagina).map(this::modelo);
    }

    // Demonstra uma consulta JPQL por nome.
    @Operation(summary = "Pesquisa pontos pelo nome usando JPQL")
    @GetMapping("/busca-nome")
    public Page<EntityModel<Saidas.Ponto>> buscarPorNome(@RequestParam String nome,
            @PageableDefault(size = 10, sort = "id") Pageable pagina) {
        return pontos.buscarPorNome(nome, pagina).map(this::modelo);
    }

    // Retorna ponto com links HATEOAS para detalhes relacionados.
    @Operation(summary = "Consulta um ponto por ID")
    @GetMapping("/{id}")
    public EntityModel<Saidas.Ponto> detalhar(@PathVariable Long id) {
        return modelo(pontos.consultar(id));
    }

    // Retorna estado calculado em tempo real, sem cache.
    @Operation(summary = "Mostra o nível da última leitura")
    @GetMapping("/{id}/situacao")
    public Saidas.Situacao situacao(@PathVariable Long id) {
        return situacao.consultar(id);
    }

    // Cadastra novo ponto (apenas administrador).
    @Operation(summary = "Cadastra um ponto de coleta")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<Saidas.Ponto>> criar(@RequestBody @Valid Entradas.Ponto dados) {
        Saidas.Ponto ponto = pontos.criar(dados);
        return ResponseEntity.created(URI.create("/api/pontos-coleta/" + ponto.id())).body(modelo(ponto));
    }

    // Atualiza dados cadastrais do ponto.
    @Operation(summary = "Edita um ponto de coleta")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<Saidas.Ponto> atualizar(@PathVariable Long id, @RequestBody @Valid Entradas.Ponto dados) {
        return modelo(pontos.atualizar(id, dados));
    }

    // Desativa sem apagar historico.
    @Operation(summary = "Desativa sem apagar o histórico")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        pontos.desativar(id);
        return ResponseEntity.noContent().build();
    }

    // Reativa um ponto removido logicamente.
    @Operation(summary = "Reativa um ponto de coleta")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public EntityModel<Saidas.Ponto> reativar(@PathVariable Long id) {
        return modelo(pontos.reativar(id));
    }

    // Inclui links públicos e ações permitidas ao administrador.
    private EntityModel<Saidas.Ponto> modelo(Saidas.Ponto ponto) {
        Long id = ponto.id();
        EntityModel<Saidas.Ponto> resposta = EntityModel.of(ponto,
            linkTo(methodOn(PontosColetaController.class).detalhar(id)).withSelfRel(),
            linkTo(PontosColetaController.class).slash(id).slash("situacao").withRel("situacao"),
            linkTo(PontosColetaController.class).slash(id).slash("leituras").withRel("leituras"));

        Authentication usuario = SecurityContextHolder.getContext().getAuthentication();
        boolean administrador = usuario != null && usuario.getAuthorities().stream()
            .anyMatch(papel -> papel.getAuthority().equals("ROLE_ADMIN"));

        if (administrador && ponto.ativo()) {
            resposta.add(linkTo(PontosColetaController.class).slash(id).withRel("editar"));
            resposta.add(linkTo(PontosColetaController.class).slash(id).withRel("desativar"));
        } else if (administrador) {
            resposta.add(linkTo(PontosColetaController.class).slash(id).slash("reativar").withRel("reativar"));
        }
        return resposta;
    }
}
