
package br.com.fiap.binova.dto;

import br.com.fiap.binova.domain.StatusOcorrencia;
import br.com.fiap.binova.domain.TipoOcorrencia;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class Entradas {

    private Entradas() {
    }

    // Dados para cadastrar um usuário
    public record Cadastro(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 8, max = 72) String senha
    ) {}

    // Dados para realizar o login
    public record Login(
            @NotBlank @Email String email,
            @NotBlank String senha
    ) {}

    // Dados para cadastrar ou atualizar um ponto de coleta
    public record Ponto(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Size(max = 255) String descricaoLocalizacao,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal capacidadeLitros
    ) {}

    // Dados enviados pelo sensor
    public record Leitura(
            @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 2) BigDecimal distanciaCm,
            @NotNull @DecimalMin("0.0") @DecimalMax("100.0") @Digits(integer = 3, fraction = 2) BigDecimal nivelPercentual
    ) {}

    // Dados para registrar uma ocorrência
    public record OcorrenciaNova(
            @NotNull @Positive Long pontoColetaId,
            @NotNull TipoOcorrencia tipo,
            @NotBlank @Size(max = 1000) String descricao
    ) {}

    // Dados para atualizar uma ocorrência
    public record OcorrenciaEdicao(
            @NotNull TipoOcorrencia tipo,
            @NotBlank @Size(max = 1000) String descricao
    ) {}

    // Dados para alterar o status de uma ocorrência
    public record Status(
            @NotNull StatusOcorrencia status
    ) {}
}
