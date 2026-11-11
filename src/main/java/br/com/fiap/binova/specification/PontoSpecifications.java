package br.com.fiap.binova.specification;

import br.com.fiap.binova.domain.PontoColeta;
import org.springframework.data.jpa.domain.Specification;

public final class PontoSpecifications {
    // Evita criar instâncias da classe de filtros.
    private PontoSpecifications() {}

    // Filtro dinamico por nome e status, usado na listagem principal.
    public static Specification<PontoColeta> filtros(String termo, Boolean ativo) {
        return (raiz, consulta, cb) -> {
            var condicoes = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (termo != null && !termo.isBlank()) {
                condicoes.add(cb.like(cb.lower(raiz.get("nome")), "%" + termo.trim().toLowerCase(java.util.Locale.ROOT) + "%"));
            }
            if (ativo != null) condicoes.add(cb.equal(raiz.get("ativo"), ativo));
            return cb.and(condicoes.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
