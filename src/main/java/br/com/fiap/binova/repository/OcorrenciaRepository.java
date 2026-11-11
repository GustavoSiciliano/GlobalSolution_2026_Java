
package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.Ocorrencia;
import br.com.fiap.binova.domain.StatusOcorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    // Lista as ocorrências de um usuário
    Page<Ocorrencia> findByUsuarioId(Long usuarioId, Pageable pageable);

    // Filtra as ocorrências de um usuário pelo status
    Page<Ocorrencia> findByUsuarioIdAndStatus(Long usuarioId, StatusOcorrencia status, Pageable pageable);

    // Lista as ocorrências de determinado status
    Page<Ocorrencia> findByStatus(StatusOcorrencia status, Pageable pageable);
}
