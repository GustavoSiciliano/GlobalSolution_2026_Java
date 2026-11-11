package br.com.fiap.binova.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ocorrencia", indexes = {
    @Index(name = "idx_ocorrencia_usuario", columnList = "usuario_id"),
    @Index(name = "idx_ocorrencia_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor
public class Ocorrencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ponto_coleta_id", nullable = false)
    private PontoColeta pontoColeta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoOcorrencia tipo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    // Insere datas obrigatorias ao abrir a ocorrencia
    @PrePersist
    void aoCriar() {
        criadoEm = OffsetDateTime.now();
        atualizadoEm = criadoEm;
    }

    // Atualiza a data quando a ocorrencia mudar.
    @PreUpdate
    void aoAlterar() {
        atualizadoEm = OffsetDateTime.now();
    }
}
