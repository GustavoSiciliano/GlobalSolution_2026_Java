package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.Ocorrencia;
import br.com.fiap.binova.domain.StatusOcorrencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long>, JpaSpecificationExecutor<Ocorrencia> {
    // Lista relatos de um cidadão.
    Page<Ocorrencia> findByUsuarioId(Long usuarioId, Pageable pageable);
    // Filtra relatos do cidadão por status.
    Page<Ocorrencia> findByUsuarioIdAndStatus(Long usuarioId, StatusOcorrencia status, Pageable pageable);
    // Filtra relatos por status.
    Page<Ocorrencia> findByStatus(StatusOcorrencia status, Pageable pageable);
}
