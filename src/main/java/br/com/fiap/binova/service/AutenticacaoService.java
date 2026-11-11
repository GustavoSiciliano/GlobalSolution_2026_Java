
package br.com.fiap.binova.service;

import br.com.fiap.binova.api.OperacaoInvalidaException;
import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.Usuario;
import br.com.fiap.binova.dto.Entradas;
import br.com.fiap.binova.dto.Saidas;
import br.com.fiap.binova.repository.UsuarioRepository;
import br.com.fiap.binova.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final AuthenticationManager autenticacao;
    private final JwtService tokens;

    // Cadastra um cidadão com a senha criptografada
    @Transactional
    public Saidas.Usuario cadastrar(Entradas.Cadastro dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);

        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new OperacaoInvalidaException("Email ja cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(senhas.encode(dados.senha()));
        usuario.setPapel(Papel.CIDADAO);

        return Saidas.Usuario.de(usuarios.saveAndFlush(usuario));
    }

    // Valida as credenciais e gera o token
    @Transactional(readOnly = true)
    public Saidas.Token login(Entradas.Login dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);

        autenticacao.authenticate(new UsernamePasswordAuthenticationToken(email, dados.senha())
        );

        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas"));

        return new Saidas.Token(
                tokens.gerar(usuario), "Bearer", tokens.validadeEmSegundos(),
                usuario.getId(), usuario.getPapel()
        );
    }

    // Recupera o usuário autenticado pelo e-mail
    @Transactional(readOnly = true)
    public Usuario usuarioAutenticado(String email) {
        return usuarios.findByEmailIgnoreCase(email).orElseThrow(() -> new BadCredentialsException("Sessao invalida"));
    }
}
