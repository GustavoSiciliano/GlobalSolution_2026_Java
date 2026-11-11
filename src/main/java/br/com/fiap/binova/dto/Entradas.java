package br.com.fiap.binova.dto;

import br.com.fiap.binova.domain.StatusOcorrencia;
import br.com.fiap.binova.domain.TipoOcorrencia;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class Entradas {
    // Evita criar instâncias desta classe de DTOs.
    private Entradas() {}

    public record Cadastro(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 72) String senha
    ) {}

    public record Login(@NotBlank @Email String email, @NotBlank String senha) {}

    public record Ponto(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 255) String descricaoLocalizacao,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal capacidadeLitros
    ) {}

    public record Leitura(
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal distanciaCm,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal nivelPercentual
    ) {}

    public record OcorrenciaNova(
        @NotNull @Positive Long pontoColetaId,
        @NotNull TipoOcorrencia tipo,
        @NotBlank @Size(max = 1000) String descricao
    ) {}

    public record OcorrenciaEdicao(
        @NotNull TipoOcorrencia tipo,
        @NotBlank @Size(max = 1000) String descricao
    ) {}

    public record Status(@NotNull StatusOcorrencia status) {}
}
