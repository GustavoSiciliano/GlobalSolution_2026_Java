package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.LeituraSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    // Lista leituras pertencentes a um ponto
    Page<LeituraSensor> findByPontoColetaId(Long pontoColetaId, Pageable pageable);

    // Encontra a leitura mais recente do ponto
    Optional<LeituraSensor> findFirstByPontoColetaIdOrderByRegistradoEmDesc(Long pontoColetaId);
}
