package br.com.fiap.binova.service;

import br.com.fiap.binova.api.OperacaoInvalidaException;
import br.com.fiap.binova.api.PermissaoNegadaException;
import br.com.fiap.binova.api.RecursoNaoEncontradoException;
import br.com.fiap.binova.domain.*;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.OcorrenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {
    private final OcorrenciaRepository ocorrencias;
    private final PontoColetaService pontos;
    private final AutenticacaoService autenticacao;

    // Admin lista todas; cidadao visualiza apenas as proprias ocorrencias.
    @Transactional(readOnly = true)
    public Page<Saidas.Ocorrencia> listar(String email, StatusOcorrencia status, Pageable pagina) {
        Usuario usuario = autenticacao.usuarioAutenticado(email);
        if (usuario.getPapel() == Papel.ADMIN) {
            return (status == null ? ocorrencias.findAll(pagina) : ocorrencias.findByStatus(status, pagina))
                .map(Saidas.Ocorrencia::de);
        }
        return (status == null ? ocorrencias.findByUsuarioId(usuario.getId(), pagina)
            : ocorrencias.findByUsuarioIdAndStatus(usuario.getId(), status, pagina))
                .map(Saidas.Ocorrencia::de);
    }

    // Restringe a consulta ao dono ou administrador.
    @Transactional(readOnly = true)
    public Saidas.Ocorrencia detalhar(Long id, String email) {
        return Saidas.Ocorrencia.de(buscarComPermissao(id, autenticacao.usuarioAutenticado(email)));
    }

    // Abre relato vinculado ao proprio usuario autenticado.
    @Transactional
    public Saidas.Ocorrencia criar(Entradas.OcorrenciaNova dados, String email) {
        Usuario usuario = autenticacao.usuarioAutenticado(email);
        PontoColeta ponto = pontos.obter(dados.pontoColetaId());
        if (!ponto.isAtivo()) throw new OperacaoInvalidaException("Nao e possivel relatar em ponto desativado");
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setUsuario(usuario);
        ocorrencia.setPontoColeta(ponto);
        ocorrencia.setTipo(dados.tipo());
        ocorrencia.setDescricao(dados.descricao().trim());
        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia));
    }

    // Altera tipo e descricao, sem liberar mudanca de autor ou status.
    @Transactional
    public Saidas.Ocorrencia atualizar(Long id, Entradas.OcorrenciaEdicao dados, String email) {
        Ocorrencia ocorrencia = buscarComPermissao(id, autenticacao.usuarioAutenticado(email));
        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA)
            throw new OperacaoInvalidaException("Ocorrencia resolvida nao pode ser editada");
        ocorrencia.setTipo(dados.tipo());
        ocorrencia.setDescricao(dados.descricao().trim());
        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia));
    }

    // Apenas ADMIN altera o fluxo de atendimento.
    @Transactional
    public Saidas.Ocorrencia mudarStatus(Long id, StatusOcorrencia novo) {
        Ocorrencia ocorrencia = obter(id);
        ocorrencia.setStatus(novo);
        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia));
    }

    // Permite excluir a propria ocorrencia enquanto estiver aberta.
    @Transactional
    public void excluir(Long id, String email) {
        Usuario usuario = autenticacao.usuarioAutenticado(email);
        Ocorrencia ocorrencia = buscarComPermissao(id, usuario);
        if (usuario.getPapel() != Papel.ADMIN && ocorrencia.getStatus() != StatusOcorrencia.ABERTA)
            throw new OperacaoInvalidaException("Somente ocorrencias abertas podem ser excluidas pelo cidadao");
        ocorrencias.delete(ocorrencia);
    }

    // Localiza a ocorrência ou retorna 404.
    private Ocorrencia obter(Long id) {
        return ocorrencias.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Ocorrencia nao encontrada: " + id));
    }

    // Rejeita acesso de outros cidadaos ao historico do autor.
    private Ocorrencia buscarComPermissao(Long id, Usuario usuario) {
        Ocorrencia ocorrencia = obter(id);
        if (usuario.getPapel() != Papel.ADMIN && !ocorrencia.getUsuario().getId().equals(usuario.getId()))
            throw new PermissaoNegadaException("Sem permissao sobre esta ocorrencia");
        return ocorrencia;
    }
}
