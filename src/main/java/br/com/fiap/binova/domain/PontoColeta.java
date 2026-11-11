package br.com.fiap.binova.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ponto_coleta")
@Getter @Setter @NoArgsConstructor
public class PontoColeta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao_localizacao", nullable = false, length = 255)
    private String descricaoLocalizacao;

    @Column(name = "capacidade_litros", nullable = false, precision = 10, scale = 2)
    private BigDecimal capacidadeLitros;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @OneToMany(mappedBy = "pontoColeta", fetch = FetchType.LAZY)
    private List<LeituraSensor> leituras = new ArrayList<>();

    @OneToMany(mappedBy = "pontoColeta", fetch = FetchType.LAZY)
    private List<Ocorrencia> ocorrencias = new ArrayList<>();

    // Registra o instante inicial do ponto
    @PrePersist
    void aoCadastrar() {
        if (criadoEm == null) criadoEm = OffsetDateTime.now();
    }
}
