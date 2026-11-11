package br.com.fiap.binova.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private CacheManager cacheManager;

    // Autentica um usuário e monta o Bearer.
    private String entrar(String email, String senha) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + campoTexto(resposta, "token");
    }

    // Recupera valores do JSON retornado para encadear os testes HTTP.
    private String campoTexto(String resposta, String campo) {
        Matcher achou = Pattern.compile("\\\"" + campo + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(resposta);
        assertTrue(achou.find(), "Campo " + campo + " nao encontrado: " + resposta);
        return achou.group(1);
    }

    // Extrai um identificador numérico da resposta.
    private long campoNumero(String resposta, String campo) {
        Matcher achou = Pattern.compile("\\\"" + campo + "\\\"\\s*:\\s*(\\d+)").matcher(resposta);
        assertTrue(achou.find(), "Campo " + campo + " nao encontrado: " + resposta);
        return Long.parseLong(achou.group(1));
    }

    // Verifica as rotas, os perfis e os códigos HTTP.
    @Test
    void autenticaValidaRegrasECrud() throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Pessoa Teste\",\"email\":\"pessoa@binova.local\",\"senha\":\"SenhaForte123!\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.papel").value("CIDADAO"));

        mvc.perform(post("/api/auth/cadastro")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Pessoa Teste\",\"email\":\"pessoa@binova.local\",\"senha\":\"SenhaForte123!\"}"))
            .andExpect(status().isConflict());

        String cidadao = entrar("pessoa@binova.local", "SenhaForte123!");
        String admin = entrar("admin@binova.local", "Admin@12345");

        mvc.perform(post("/api/pontos-coleta").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Teste\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":50}"))
            .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/pontos-coleta").header("Authorization", cidadao)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Teste\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":50}"))
            .andExpect(status().isForbidden());

        String pontoJson = mvc.perform(post("/api/pontos-coleta").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Lixeira Teste\",\"descricaoLocalizacao\":\"Hall Central\",\"capacidadeLitros\":50}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long pontoId = campoNumero(pontoJson, "id");
        assertTrue(pontoId > 0);

        // Público vê detalhes, sem receber links administrativos.
        mvc.perform(get("/api/pontos-coleta/" + pontoId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.desativar").doesNotExist())
            .andExpect(jsonPath("$._links.reativar").doesNotExist());

        // Apenas contas autenticadas acessam o histórico de leituras.
        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/leituras"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/leituras")
                .header("Authorization", cidadao))
            .andExpect(status().isOk());

        // Os detalhes entram em cache após a primeira consulta.
        assertNotNull(cacheManager.getCache("pontos").get(pontoId));

        // O administrador recebe os links das operações permitidas.
        mvc.perform(get("/api/pontos-coleta/" + pontoId).header("Authorization", admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.desativar.href").exists())
            .andExpect(jsonPath("$._links.reativar").doesNotExist());

        mvc.perform(get("/api/pontos-coleta/999999"))
            .andExpect(status().isNotFound());

        mvc.perform(post("/api/pontos-coleta/" + pontoId + "/leituras").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"distanciaCm\":10,\"nivelPercentual\":120}"))
            .andExpect(status().isBadRequest());

        mvc.perform(post("/api/pontos-coleta/" + pontoId + "/leituras").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"distanciaCm\":10,\"nivelPercentual\":85}"))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/situacao"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("PROXIMA_DO_LIMITE"));

        String ocorrenciaJson = mvc.perform(post("/api/ocorrencias").header("Authorization", cidadao)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"pontoColetaId\":" + pontoId + ",\"tipo\":\"LIXEIRA_CHEIA\",\"descricao\":\"Encheu\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long ocorrenciaId = campoNumero(ocorrenciaJson, "id");
        assertTrue(ocorrenciaId > 0);

        // O cidadão não pode alterar o status, nem recebe esse link.
        mvc.perform(get("/api/ocorrencias/" + ocorrenciaId).header("Authorization", cidadao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.alterar-status").doesNotExist());
        mvc.perform(patch("/api/ocorrencias/" + ocorrenciaId + "/status").header("Authorization", cidadao)
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"EM_ATENDIMENTO\"}"))
            .andExpect(status().isForbidden());

        mvc.perform(patch("/api/ocorrencias/" + ocorrenciaId + "/status").header("Authorization", admin)
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"RESOLVIDA\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RESOLVIDA"));

        // Consulta derivada, JPQL e Specification com filtros.
        mvc.perform(get("/api/pontos-coleta").param("ativo", "true"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/pontos-coleta/busca-nome").param("nome", "Lixeira"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/pontos-coleta").param("ativo", "true").param("termo", "Lixeira"))
            .andExpect(status().isOk());

        // CRUD de pontos: atualização, exclusão lógica e reativação.
        mvc.perform(put("/api/pontos-coleta/" + pontoId).header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ponto Atualizado\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":60}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ponto Atualizado"));
        // A escrita invalida o cache antigo.
        assertNull(cacheManager.getCache("pontos").get(pontoId));
        mvc.perform(delete("/api/pontos-coleta/" + pontoId).header("Authorization", admin))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/pontos-coleta/" + pontoId))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mvc.perform(patch("/api/pontos-coleta/" + pontoId + "/reativar")
                .header("Authorization", admin))
            .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(true));
    }
}
