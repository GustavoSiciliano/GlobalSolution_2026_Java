package br.com.fiap.binova.security;

import br.com.fiap.binova.domain.Papel;
import br.com.fiap.binova.domain.Usuario;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SEGREDO = Base64.getEncoder().encodeToString(
        "Segredo-da-equipe-Binova-Testes-2026-com-mais-de-32-bytes".getBytes());

    // Valida a assinatura e a validade do JWT
    @Test
    void emiteEValidaTokenAssinado() {
        JwtService jwt = new JwtService(SEGREDO, 30);
        Usuario usuario = new Usuario();
        usuario.setEmail("cidadao@binova.local");
        usuario.setPapel(Papel.CIDADAO);
        assertEquals("cidadao@binova.local", jwt.extrairEmail(jwt.gerar(usuario)));
        assertEquals(1800, jwt.validadeEmSegundos());
    }

    // Rejeita uma chave fraca para assinatura
    @Test
    void rejeitaChaveCurta() {
        String curto = Base64.getEncoder().encodeToString("123".getBytes());
        assertThrows(IllegalStateException.class, () -> new JwtService(curto, 30));
    }
}
