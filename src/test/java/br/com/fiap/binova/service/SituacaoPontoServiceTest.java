
package br.com.fiap.binova.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SituacaoPontoServiceTest {

    private final Clock relogio = Clock.fixed(
            Instant.parse("2026-11-10T15:00:00Z"), ZoneOffset.UTC);

    private final OffsetDateTime recente = OffsetDateTime.parse("2026-11-10T14:59:00Z");

    // Testa os limites de classificação do preenchimento
    @Test
    void classificaFaixasDePercentual() {
        assertEquals("DISPONIVEL", SituacaoPontoService.classificar(new BigDecimal("0"), recente, relogio));
        assertEquals("DISPONIVEL", SituacaoPontoService.classificar(new BigDecimal("49.99"), recente, relogio));
        assertEquals("ATENCAO", SituacaoPontoService.classificar(new BigDecimal("50"), recente, relogio));
        assertEquals("ATENCAO", SituacaoPontoService.classificar(new BigDecimal("79.99"), recente, relogio));
        assertEquals("PROXIMA_DO_LIMITE", SituacaoPontoService.classificar(new BigDecimal("80"), recente, relogio));
        assertEquals("PROXIMA_DO_LIMITE", SituacaoPontoService.classificar(new BigDecimal("100"), recente, relogio));
    }

    // Testa leituras antigas, futuras e percentuais inválidos
    @Test
    void rejeitaDadosDesatualizadosOuInvalidos() {
        assertEquals("PROXIMA_DO_LIMITE", SituacaoPontoService.classificar(new BigDecimal("90"),
                OffsetDateTime.parse("2026-11-10T14:45:00Z"), relogio));

        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(new BigDecimal("90"),
                OffsetDateTime.parse("2026-11-10T14:44:00Z"), relogio));

        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(new BigDecimal("90"),
                OffsetDateTime.parse("2026-11-10T15:01:00Z"), relogio));

        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(null, recente, relogio));
        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(new BigDecimal("-1"), recente, relogio));
        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(new BigDecimal("101"), recente, relogio));
        assertEquals("SEM_DADOS_ATUALIZADOS", SituacaoPontoService.classificar(new BigDecimal("50"), null, relogio));
    }
}
