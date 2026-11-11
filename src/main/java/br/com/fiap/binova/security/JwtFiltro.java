
package br.com.fiap.binova.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserDetailsService usuarios;

    // Valida o JWT e autentica o usuário da requisição
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                String token = cabecalho.substring(7);
                String email = jwt.extrairEmail(token);

                var usuario = usuarios.loadUserByUsername(email);

                var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities()
                );

                autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(autenticacao);

            } catch (JwtException | IllegalArgumentException |
                     org.springframework.security.core.userdetails.UsernameNotFoundException erro) {

                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }
}
