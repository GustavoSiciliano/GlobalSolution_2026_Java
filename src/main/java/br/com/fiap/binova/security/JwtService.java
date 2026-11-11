
package br.com.fiap.binova.security;

import br.com.fiap.binova.domain.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey chave;
    private final long minutos;

    // Configura a chave e o tempo de validade
    public JwtService(
            @Value("${app.jwt.secret-base64:}") String segredoBase64,
            @Value("${app.jwt.minutos:120}") long minutos) {

        if (minutos < 1 || minutos > 1440) {
            throw new IllegalStateException("Validade do JWT deve ficar entre 1 e 1440 minutos");
        }

        if (segredoBase64 == null || segredoBase64.isBlank()) {
            byte[] chaveLocal = new byte[32];
            new SecureRandom().nextBytes(chaveLocal);

            this.chave = Keys.hmacShaKeyFor(chaveLocal);
            log.warn("Chave JWT temporaria criada para desenvolvimento");
        } else {

            try {
                byte[] dados = Decoders.BASE64.decode(segredoBase64);

                if (dados.length < 32) {
                    throw new IllegalArgumentException("Chave deve conter pelo menos 32 bytes");
                }

                this.chave = Keys.hmacShaKeyFor(dados);

            } catch (RuntimeException erro) {
                throw new IllegalStateException("Chave JWT invalida", erro);
            }
        }

        this.minutos = minutos;
    }

    // Gera um token JWT para o usuário autenticado
    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(usuario.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(minutos * 60)))
                .claim("papel", usuario.getPapel().name())
                .signWith(chave)
                .compact();
    }

    // Valida o token e recupera o e-mail
    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Retorna a validade do token em segundos
    public long validadeEmSegundos() {
        return minutos * 60;
    }
}
