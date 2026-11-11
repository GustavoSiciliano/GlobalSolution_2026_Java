package br.com.fiap.binova.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leitura_sensor", indexes = @Index(name = "idx_leitura_ponto_data", columnList = "ponto_coleta_id,registrado_em"))
@Getter @Setter @NoArgsConstructor
public class LeituraSensor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ponto_coleta_id", nullable = false)
    private PontoColeta pontoColeta;

    @Column(name = "distancia_cm", nullable = false, precision = 10, scale = 2)
    private BigDecimal distanciaCm;

    @Column(name = "nivel_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal nivelPercentual;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private OffsetDateTime registradoEm;

    // Usa o horario do servidor
    @PrePersist
    void aoRegistrar() {
        if (registradoEm == null) registradoEm = OffsetDateTime.now();
    }
}
