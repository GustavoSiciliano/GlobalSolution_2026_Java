package br.com.fiap.binova.dto;

import br.com.fiap.binova.domain.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class Saidas {

    private Saidas() {}

    public record Usuario(Long id, String nome, String email, Papel papel, OffsetDateTime criadoEm) {
        // Converte entidade em dados públicos do usuário
        public static Usuario de(br.com.fiap.binova.domain.Usuario usuario) {
            return new Usuario(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(), usuario.getCriadoEm());
        }
    }

    public record Token(String token, String tipo, long expiraEmSegundos, Long usuarioId, Papel papel) {}

    public record Ponto(Long id, String nome, String descricaoLocalizacao, BigDecimal capacidadeLitros,
                        boolean ativo, OffsetDateTime criadoEm) {

        // Converte o ponto em dados de resposta
        public static Ponto de(PontoColeta ponto) {
            return new Ponto(ponto.getId(), ponto.getNome(), ponto.getDescricaoLocalizacao(),
                ponto.getCapacidadeLitros(), ponto.isAtivo(), ponto.getCriadoEm());
        }
    }

    public record Leitura(Long id, Long pontoColetaId, BigDecimal distanciaCm,
                         BigDecimal nivelPercentual, OffsetDateTime registradoEm) {

        // Converte uma leitura em dados de resposta
        public static Leitura de(LeituraSensor leitura) {
            return new Leitura(leitura.getId(), leitura.getPontoColeta().getId(),
                leitura.getDistanciaCm(), leitura.getNivelPercentual(), leitura.getRegistradoEm());
        }
    }

    public record Ocorrencia(Long id, Long pontoColetaId, Long usuarioId, TipoOcorrencia tipo,
                            String descricao, StatusOcorrencia status, OffsetDateTime criadoEm,
                            OffsetDateTime atualizadoEm) {

        // Converte ocorrência em dados de resposta
        public static Ocorrencia de(br.com.fiap.binova.domain.Ocorrencia ocorrencia) {
            return new Ocorrencia(ocorrencia.getId(), ocorrencia.getPontoColeta().getId(),
                ocorrencia.getUsuario().getId(), ocorrencia.getTipo(), ocorrencia.getDescricao(),
                ocorrencia.getStatus(), ocorrencia.getCriadoEm(), ocorrencia.getAtualizadoEm());
        }
    }

    public record Situacao(Long pontoColetaId, String situacao, BigDecimal nivelPercentual,
                          OffsetDateTime ultimaLeitura, long limiteMinutos) {}
}
