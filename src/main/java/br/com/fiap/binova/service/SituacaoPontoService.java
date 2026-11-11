package br.com.fiap.binova.service;

import br.com.fiap.binova.domain.LeituraSensor;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.LeituraSensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SituacaoPontoService {

    private static final long LIMITE_MINUTOS = 15;
    private final LeituraSensorRepository leituras;
    private final PontoColetaService pontos;
    private final Clock clock;

    // Mostra o estado calculado a partir da ultima leitura do ponto
    @Transactional(readOnly = true)
    public Saidas.Situacao consultar(Long pontoId) {

        pontos.obter(pontoId);
        Optional<LeituraSensor> ultima = leituras.findFirstByPontoColetaIdOrderByRegistradoEmDesc(pontoId);

        if (ultima.isEmpty())
            return new Saidas.Situacao(pontoId, "SEM_DADOS_ATUALIZADOS", null, null, LIMITE_MINUTOS);
        LeituraSensor leitura = ultima.get();

        return new Saidas.Situacao(pontoId,  classificar(leitura.getNivelPercentual(), leitura.getRegistradoEm(), clock),
            leitura.getNivelPercentual(), leitura.getRegistradoEm(), LIMITE_MINUTOS);
    }

    // Preenchimento
    public static String classificar(BigDecimal nivel, OffsetDateTime instante, Clock relogio) {

        if (nivel == null || instante == null || nivel.compareTo(BigDecimal.ZERO) < 0 ||  nivel.compareTo(new BigDecimal("100")) > 0 ||
                instante.toInstant().isAfter(relogio.instant()) | Duration.between(instante.toInstant(), relogio.instant()).compareTo(Duration.ofMinutes(LIMITE_MINUTOS)) > 0)
            return "SEM_DADOS_ATUALIZADOS";

        if (nivel.compareTo(new BigDecimal("50")) < 0)
            return "DISPONIVEL";
        if (nivel.compareTo(new BigDecimal("80")) < 0)
            return "ATENCAO";
        return "PROXIMA_DO_LIMITE";
    }
}
