
package br.com.fiap.binova.service;

import br.com.fiap.binova.api.OperacaoInvalidaException;
import br.com.fiap.binova.api.RecursoNaoEncontradoException;
import br.com.fiap.binova.domain.LeituraSensor;
import br.com.fiap.binova.domain.PontoColeta;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.LeituraSensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeituraSensorService {

    private final LeituraSensorRepository leituras;
    private final PontoColetaService pontos;

    // Lista o histórico de leituras de um ponto
    @Transactional(readOnly = true)
    public Page<Saidas.Leitura> listar(Long pontoId, Pageable pagina) {
        pontos.obter(pontoId);

        return leituras.findByPontoColetaId(pontoId, pagina).map(Saidas.Leitura::de);
    }

    // Registra uma leitura em um ponto ativo
    @Transactional
    public Saidas.Leitura registrar(Long pontoId, Entradas.Leitura dados) {
        PontoColeta ponto = pontos.obter(pontoId);

        if (!ponto.isAtivo()) {
            throw new OperacaoInvalidaException("Ponto desativado nao recebe leituras");
        }

        LeituraSensor leitura = new LeituraSensor();
        leitura.setPontoColeta(ponto);
        leitura.setDistanciaCm(dados.distanciaCm());
        leitura.setNivelPercentual(dados.nivelPercentual());

        return Saidas.Leitura.de(leituras.saveAndFlush(leitura));
    }

    // Consulta uma leitura pertencente ao ponto informado
    @Transactional(readOnly = true)
    public Saidas.Leitura detalhar(Long pontoId, Long leituraId) {
        pontos.obter(pontoId);

        LeituraSensor leitura = leituras.findById(leituraId).orElseThrow(() ->
                        new RecursoNaoEncontradoException("Leitura nao encontrada")
                );

        if (!leitura.getPontoColeta().getId().equals(pontoId)) {
            throw new RecursoNaoEncontradoException("Leitura nao pertence ao ponto informado");
        }

        return Saidas.Leitura.de(leitura);
    }
}
