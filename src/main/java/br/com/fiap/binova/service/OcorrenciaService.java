
package br.com.fiap.binova.service;

import br.com.fiap.binova.api.OperacaoInvalidaException;
import br.com.fiap.binova.api.PermissaoNegadaException;
import br.com.fiap.binova.api.RecursoNaoEncontradoException;
import br.com.fiap.binova.domain.Ocorrencia;
import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.PontoColeta;
import br.com.fiap.binova.domain.StatusOcorrencia;
import br.com.fiap.binova.domain.Usuario;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.OcorrenciaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    private final OcorrenciaRepository ocorrencias;
    private final PontoColetaService pontos;
    private final AutenticacaoService autenticacao;

    // Lista ocorrências conforme o usuário e o status
    @Transactional(readOnly = true)
    public Page<Saidas.Ocorrencia> listar(String email, StatusOcorrencia status,
            Pageable pagina) {

        Usuario usuario = autenticacao.usuarioAutenticado(email);

        if (usuario.getPapel() == Papel.ADMIN) {
            if (status == null) {
                return ocorrencias.findAll(pagina).map(Saidas.Ocorrencia::de);
            }

            return ocorrencias.findByStatus(status, pagina).map(Saidas.Ocorrencia::de);
        }

        if (status == null) {
            return ocorrencias.findByUsuarioId(usuario.getId(), pagina)
                    .map(Saidas.Ocorrencia::de);
        }

        return ocorrencias.findByUsuarioIdAndStatus(usuario.getId(), status, pagina)
                .map(Saidas.Ocorrencia::de);
    }

    // Consulta uma ocorrência com verificação de permissão
    @Transactional(readOnly = true)
    public Saidas.Ocorrencia detalhar(Long id, String email) {
        Usuario usuario = autenticacao.usuarioAutenticado(email);
        Ocorrencia ocorrencia = buscarComPermissao(id, usuario);

        return Saidas.Ocorrencia.de(ocorrencia);
    }

    // Registra uma nova ocorrência para o cidadão
    @Transactional
    public Saidas.Ocorrencia criar(Entradas.OcorrenciaNova dados, String email) {

        Usuario usuario = autenticacao.usuarioAutenticado(email);
        PontoColeta ponto = pontos.obter(dados.pontoColetaId());

        if (!ponto.isAtivo()) {
            throw new OperacaoInvalidaException("Nao e possivel relatar em ponto desativado");
        }

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setUsuario(usuario);
        ocorrencia.setPontoColeta(ponto);
        ocorrencia.setTipo(dados.tipo());
        ocorrencia.setDescricao(dados.descricao().trim());

        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia)
        );
    }

    // Atualiza o tipo e a descrição de uma ocorrência
    @Transactional
    public Saidas.Ocorrencia atualizar(Long id, Entradas.OcorrenciaEdicao dados, String email) {

        Usuario usuario = autenticacao.usuarioAutenticado(email);
        Ocorrencia ocorrencia = buscarComPermissao(id, usuario);

        if (ocorrencia.getStatus() == StatusOcorrencia.RESOLVIDA) {
            throw new OperacaoInvalidaException("Ocorrencia resolvida nao pode ser editada");
        }

        ocorrencia.setTipo(dados.tipo());
        ocorrencia.setDescricao(dados.descricao().trim());

        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia)
        );
    }

    // Altera o status de atendimento como administrador
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Saidas.Ocorrencia mudarStatus(Long id, StatusOcorrencia novo) {

        Ocorrencia ocorrencia = obter(id);
        ocorrencia.setStatus(novo);

        return Saidas.Ocorrencia.de(ocorrencias.saveAndFlush(ocorrencia)
        );
    }

    // Exclui uma ocorrência conforme as regras de acesso
    @Transactional
    public void excluir(Long id, String email) {

        Usuario usuario = autenticacao.usuarioAutenticado(email);
        Ocorrencia ocorrencia = buscarComPermissao(id, usuario);

        if (usuario.getPapel() != Papel.ADMIN && ocorrencia.getStatus() != StatusOcorrencia.ABERTA) {

            throw new OperacaoInvalidaException("Somente ocorrencias abertas podem ser excluidas pelo cidadao");
        }

        ocorrencias.delete(ocorrencia);
    }

    // Busca uma ocorrência ou retorna erro 404
    private Ocorrencia obter(Long id) {
        return ocorrencias.findById(id).orElseThrow(()
                        -> new RecursoNaoEncontradoException("Ocorrencia nao encontrada: " + id)
                );
    }

    // Verifica se o usuário pode acessar a ocorrência
    private Ocorrencia buscarComPermissao(Long id, Usuario usuario) {

        Ocorrencia ocorrencia = obter(id);

        boolean administrador = usuario.getPapel() == Papel.ADMIN;
        boolean autor = ocorrencia.getUsuario().getId().equals(usuario.getId());

        if (!administrador && !autor) {
            throw new PermissaoNegadaException("Sem permissao sobre esta ocorrencia");
        }

        return ocorrencia;
    }
}
