package br.com.fiap.binova.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.service.LeituraSensorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pontos-coleta/{pontoId}/leituras")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Leituras do sensor")
public class LeiturasController {
    private final LeituraSensorService leituras;

    // Consulta o historico de medidas, inclusive as antigas
    @Operation(summary = "Lista leituras de um ponto")
    @GetMapping
    public Page<EntityModel<Saidas.Leitura>> listar(@PathVariable Long pontoId,
            @PageableDefault(size = 10, sort = "registradoEm", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pagina) {
        return leituras.listar(pontoId, pagina).map(leitura -> modelo(pontoId, leitura));
    }

    // Busca uma leitura especifica do ponto
    @Operation(summary = "Consulta uma leitura específica")
    @GetMapping("/{id}")
    public EntityModel<Saidas.Leitura> detalhar(@PathVariable Long pontoId, @PathVariable Long id) {
        return modelo(pontoId, leituras.detalhar(pontoId, id));
    }

    // Recebe medicao de dispositivo com credencial de administrador
    @Operation(summary = "Registra uma leitura de sensor")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<Saidas.Leitura>> registrar(@PathVariable Long pontoId,
            @RequestBody @Valid Entradas.Leitura dados) {
        Saidas.Leitura leitura = leituras.registrar(pontoId, dados);
        return ResponseEntity.created(URI.create("/api/pontos-coleta/" + pontoId + "/leituras/" + leitura.id()))
            .body(modelo(pontoId, leitura));
    }

    // Inclui links para a leitura e o ponto relacionado
    private EntityModel<Saidas.Leitura> modelo(Long pontoId, Saidas.Leitura leitura) {
        return EntityModel.of(leitura,
            linkTo(methodOn(LeiturasController.class).detalhar(pontoId, leitura.id())).withSelfRel(),
            linkTo(PontosColetaController.class).slash(pontoId).withRel("ponto"));
    }
}
