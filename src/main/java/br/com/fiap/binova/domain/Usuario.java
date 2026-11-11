package br.com.fiap.binova.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuario", indexes = @Index(name = "idx_usuario_email", columnList = "email", unique = true))
@Getter @Setter @NoArgsConstructor
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Papel papel = Papel.CIDADAO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY)
    private List<Ocorrencia> ocorrencias = new ArrayList<>();

    // Preenche a data de cadastro somente na primeira gravacao.
    @PrePersist
    void aoCadastrar() {
        if (criadoEm == null) criadoEm = OffsetDateTime.now();
    }
}
