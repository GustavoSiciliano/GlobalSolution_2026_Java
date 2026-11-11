
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

    // Consulta a situação atual do ponto de coleta
    @Transactional(readOnly = true)
    public Saidas.Situacao consultar(Long pontoId) {

        pontos.obter(pontoId);

        Optional<LeituraSensor> ultima = leituras.findFirstByPontoColetaIdOrderByRegistradoEmDesc(pontoId);

        if (ultima.isEmpty()) {
            return new Saidas.Situacao(pontoId, "SEM_DADOS_ATUALIZADOS", null, null, LIMITE_MINUTOS
            );
        }

        LeituraSensor leitura = ultima.get();

        String situacao = classificar(leitura.getNivelPercentual(),
                leitura.getRegistradoEm(), clock
        );

        return new Saidas.Situacao(pontoId, situacao, leitura.getNivelPercentual(),
                leitura.getRegistradoEm(), LIMITE_MINUTOS
        );
    }

    // Classifica o preenchimento e a validade da leitura
    public static String classificar(BigDecimal nivel, OffsetDateTime instante, Clock relogio) {

        if (nivel == null || instante == null) {
            return "SEM_DADOS_ATUALIZADOS";
        }

        if (nivel.compareTo(BigDecimal.ZERO) < 0 ||
                nivel.compareTo(BigDecimal.valueOf(100)) > 0) {
            return "SEM_DADOS_ATUALIZADOS";
        }

        var agora = relogio.instant();
        var dataLeitura = instante.toInstant();

        if (dataLeitura.isAfter(agora) || Duration.between(dataLeitura, agora)
                .compareTo(Duration.ofMinutes(LIMITE_MINUTOS)) > 0) {
            return "SEM_DADOS_ATUALIZADOS";
        }

        if (nivel.compareTo(BigDecimal.valueOf(50)) < 0) {
            return "DISPONIVEL";
        }

        if (nivel.compareTo(BigDecimal.valueOf(80)) < 0) {
            return "ATENCAO";
        }

        return "PROXIMA_DO_LIMITE";
    }
}
