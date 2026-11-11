package br.com.fiap.binova.service;

import br.com.fiap.binova.api.RecursoNaoEncontradoException;
import br.com.fiap.binova.domain.PontoColeta;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.PontoColetaRepository;
import br.com.fiap.binova.specification.PontoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PontoColetaService {
    private final PontoColetaRepository pontos;

    // Lista os pontos aplicando Specification
    @Transactional(readOnly = true)
    public Page<Saidas.Ponto> listar(String termo, Boolean ativo, Pageable pagina) {

        if (Boolean.TRUE.equals(ativo) && (termo == null || termo.isBlank())) {
            return pontos.findByAtivoTrue(pagina).map(Saidas.Ponto::de);
        }
        return pontos.findAll(PontoSpecifications.filtros(termo, ativo), pagina).map(Saidas.Ponto::de);
    }

    // Executa a consulta JPQL
    @Transactional(readOnly = true)
    public Page<Saidas.Ponto> buscarPorNome(String nome, Pageable pagina) {
        return pontos.buscarPorNome(nome, pagina).map(Saidas.Ponto::de);
    }

    // Busca ponto por ID e guarda detalhes estaveis em cache
    @Cacheable(value = "pontos", key = "#id")
    @Transactional(readOnly = true)
    public Saidas.Ponto consultar(Long id) {
        return Saidas.Ponto.de(obter(id));
    }

    // Recupera entidade existente para validacoes internas
    @Transactional(readOnly = true)
    public PontoColeta obter(Long id) {
        return pontos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Ponto nao encontrado: " + id));
    }

    // Cria ponto e invalida o cache de detalhes
    @CacheEvict(value = "pontos", allEntries = true)
    @Transactional
    public Saidas.Ponto criar(Entradas.Ponto dados) {
        PontoColeta ponto = new PontoColeta();
        preencher(ponto, dados);
        return Saidas.Ponto.de(pontos.saveAndFlush(ponto));
    }

    // Atualiza dados do ponto e limpa o cache antigo
    @CacheEvict(value = "pontos", allEntries = true)
    @Transactional
    public Saidas.Ponto atualizar(Long id, Entradas.Ponto dados) {
        PontoColeta ponto = obter(id);
        preencher(ponto, dados);
        return Saidas.Ponto.de(pontos.saveAndFlush(ponto));
    }

    // Exclui logicamente um ponto, preservando leituras e ocorrencias
    @CacheEvict(value = "pontos", allEntries = true)
    @Transactional
    public void desativar(Long id) {
        PontoColeta ponto = obter(id);
        ponto.setAtivo(false);
        pontos.save(ponto);
    }

    // Permite reativacao administrativa sem apagar historico
    @CacheEvict(value = "pontos", allEntries = true)
    @Transactional
    public Saidas.Ponto reativar(Long id) {
        PontoColeta ponto = obter(id);
        ponto.setAtivo(true);
        return Saidas.Ponto.de(pontos.saveAndFlush(ponto));
    }

    // Preenche os campos recebidos no formulário
    private void preencher(PontoColeta ponto, Entradas.Ponto dados) {
        ponto.setNome(dados.nome().trim());
        ponto.setDescricaoLocalizacao(dados.descricaoLocalizacao().trim());
        ponto.setCapacidadeLitros(dados.capacidadeLitros());
    }
}
