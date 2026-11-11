
package br.com.fiap.binova.specification;

import br.com.fiap.binova.domain.PontoColeta;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PontoSpecifications {

    private PontoSpecifications() {
    }

    // Filtra pontos de coleta por nome e situação.
    public static Specification<PontoColeta> filtros(String termo, Boolean ativo) {
        return (raiz, consulta, cb) -> {

            List<Predicate> condicoes = new ArrayList<>();

            if (termo != null && !termo.isBlank()) {
                String nome = "%" + termo.trim().toLowerCase(Locale.ROOT) + "%";

                condicoes.add(
                        cb.like(cb.lower(raiz.get("nome")), nome)
                );
            }

            if (ativo != null) {
                condicoes.add(cb.equal(raiz.get("ativo"), ativo));
            }

            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}
