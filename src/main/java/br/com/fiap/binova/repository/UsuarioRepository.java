
package br.com.fiap.binova.repository;

import br.com.fiap.binova.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca um usuário pelo e-mail
    Optional<Usuario> findByEmailIgnoreCase(String email);

    // Verifica se o e-mail já está cadastrado
    boolean existsByEmailIgnoreCase(String email);
}
