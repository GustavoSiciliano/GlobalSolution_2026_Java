
package br.com.fiap.binova.security;

import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.Usuario;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SEGREDO = Base64.getEncoder().encodeToString(
            "Segredo-da-equipe-Binova-Testes-2026-com-mais-de-32-bytes"
                    .getBytes(StandardCharsets.UTF_8));

    // Verifica a geração e validação do JWT
    @Test
    void emiteEValidaTokenAssinado() {
        JwtService jwt = new JwtService(SEGREDO, 30);

        Usuario usuario = new Usuario();
        usuario.setEmail("cidadao@binova.local");
        usuario.setPapel(Papel.CIDADAO);

        String token = jwt.gerar(usuario);

        assertEquals("cidadao@binova.local", jwt.extrairEmail(token));
        assertEquals(1800, jwt.validadeEmSegundos());
    }

    // Verifica uma chave de assinatura
    @Test
    void rejeitaChaveCurta() {
        String curto = Base64.getEncoder().encodeToString(
                "123".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> new JwtService(curto, 30));
    }
}
