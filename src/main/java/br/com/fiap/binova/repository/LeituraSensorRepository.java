
package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.LeituraSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {

    // Lista as leituras de um ponto com paginação
    Page<LeituraSensor> findByPontoColetaId(Long pontoColetaId, Pageable pageable);

    // Busca a leitura mais recente de um ponto
    Optional<LeituraSensor> findFirstByPontoColetaIdOrderByRegistradoEmDesc(Long pontoColetaId);
}
