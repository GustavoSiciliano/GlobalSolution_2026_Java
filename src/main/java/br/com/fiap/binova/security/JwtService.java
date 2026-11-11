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

    // Valida a chave configurada ou cria uma chave temporaria para uso local.
    public JwtService(@Value("${app.jwt.secret-base64:}") String segredoBase64,
                      @Value("${app.jwt.minutos:120}") long minutos) {
        if (minutos < 1 || minutos > 1440) {
            throw new IllegalStateException("Tempo do JWT deve ficar entre 1 e 1440 minutos");
        }
        if (segredoBase64 == null || segredoBase64.isBlank()) {
            byte[] chaveLocal = new byte[32];
            new SecureRandom().nextBytes(chaveLocal);
            this.chave = Keys.hmacShaKeyFor(chaveLocal);
            log.warn("JWT_SECRET_BASE64 ausente: chave temporaria gerada (tokens expiram ao reiniciar)");
        } else {
            try {
                byte[] dados = Decoders.BASE64.decode(segredoBase64);
                if (dados.length < 32) {
                    throw new IllegalArgumentException("A chave deve conter pelo menos 32 bytes");
                }
                this.chave = Keys.hmacShaKeyFor(dados);
            } catch (RuntimeException erro) {
                throw new IllegalStateException("JWT_SECRET_BASE64 deve conter Base64 de pelo menos 32 bytes", erro);
            }
        }
        this.minutos = minutos;
    }

    // Gera token assinado para o usuario autenticado.
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

    // Extrai o email somente de um token valido.
    public String extrairEmail(String token) {
        return Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload().getSubject();
    }

    // Converte a validade do token para segundos.
    public long validadeEmSegundos() {
        return minutos * 60;
    }
}
