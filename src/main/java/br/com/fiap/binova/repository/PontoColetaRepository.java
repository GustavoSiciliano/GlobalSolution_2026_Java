package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.PontoColeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PontoColetaRepository extends JpaRepository<PontoColeta, Long>, JpaSpecificationExecutor<PontoColeta> {
    // Query method derivado: pontos ativos.
    Page<PontoColeta> findByAtivoTrue(Pageable pageable);

    // JPQL: busca textual pelo nome independentemente de maiusculas.
    @Query("select p from PontoColeta p where lower(p.nome) like lower(concat('%', :nome, '%'))")
    Page<PontoColeta> buscarPorNome(@Param("nome") String nome, Pageable pageable);
}
