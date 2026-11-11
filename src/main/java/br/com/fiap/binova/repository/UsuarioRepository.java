package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca usuário pelo email, ignorando maiúsculas.
    Optional<Usuario> findByEmailIgnoreCase(String email);
    // Verifica se o email já foi cadastrado.
    boolean existsByEmailIgnoreCase(String email);
}
