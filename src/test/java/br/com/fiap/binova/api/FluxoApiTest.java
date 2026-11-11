
package br.com.fiap.binova.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:binova_fluxo_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.demo.enabled=true",
        "app.demo.admin.password=Admin@12345",
        "app.demo.cidadao.password=Cidadao@12345"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CacheManager cacheManager;

    // Realiza o login e retorna o token Bearer.
    private String entrar(String email, String senha) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return "Bearer " + campoTexto(resposta, "token");
    }

    // Recupera um campo de texto da resposta JSON
    private String campoTexto(String resposta, String campo) {
        return JsonPath.read(resposta, "$." + campo);
    }

    // Recupera um identificador numérico da resposta JSON
    private long campoNumero(String resposta, String campo) {
        Number numero = JsonPath.read(resposta, "$." + campo);
        return numero.longValue();
    }

    // Verifica autenticação, permissões, leituras, ocorrências e CRUD
    @Test
    void autenticaValidaRegrasECrud() throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pessoa Teste\",\"email\":\"pessoa@binova.local\",\"senha\":\"SenhaForte123!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.papel").value("CIDADAO"));

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Pessoa Teste\",\"email\":\"pessoa@binova.local\",\"senha\":\"SenhaForte123!\"}"))
                .andExpect(status().isConflict());

        String cidadao = entrar("pessoa@binova.local", "SenhaForte123!");
        String admin = entrar("admin@binova.local", "Admin@12345");

        // Verifica o acesso ao cadastro de pontos.
        mvc.perform(post("/api/pontos-coleta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":50}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/pontos-coleta")
                        .header("Authorization", cidadao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Teste\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":50}"))
                .andExpect(status().isForbidden());

        String pontoJson = mvc.perform(post("/api/pontos-coleta")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Lixeira Teste\",\"descricaoLocalizacao\":\"Hall Central\",\"capacidadeLitros\":50}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long pontoId = campoNumero(pontoJson, "id");
        assertTrue(pontoId > 0);

        // Verifica os links públicos e o histórico de leituras
        mvc.perform(get("/api/pontos-coleta/" + pontoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.desativar").doesNotExist())
                .andExpect(jsonPath("$._links.reativar").doesNotExist());

        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/leituras"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/leituras")
                        .header("Authorization", cidadao))
                .andExpect(status().isOk());

        // Verifica o cache e os links administrativos
        assertNotNull(cacheManager.getCache("pontos"));
        assertNotNull(cacheManager.getCache("pontos").get(pontoId));

        mvc.perform(get("/api/pontos-coleta/" + pontoId)
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.desativar.href").exists())
                .andExpect(jsonPath("$._links.reativar").doesNotExist());

        mvc.perform(get("/api/pontos-coleta/999999"))
                .andExpect(status().isNotFound());

        // Verifica a validação e o cadastro das leituras
        mvc.perform(post("/api/pontos-coleta/" + pontoId + "/leituras")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanciaCm\":10,\"nivelPercentual\":120}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/pontos-coleta/" + pontoId + "/leituras")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"distanciaCm\":10,\"nivelPercentual\":85}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/pontos-coleta/" + pontoId + "/situacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PROXIMA_DO_LIMITE"));

        // Registra uma ocorrência como cidadão
        String ocorrenciaJson = mvc.perform(post("/api/ocorrencias")
                        .header("Authorization", cidadao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pontoColetaId\":" + pontoId + ",\"tipo\":\"LIXEIRA_CHEIA\",\"descricao\":\"Encheu\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long ocorrenciaId = campoNumero(ocorrenciaJson, "id");
        assertTrue(ocorrenciaId > 0);

        // Verifica as permissões para alterar o status
        mvc.perform(get("/api/ocorrencias/" + ocorrenciaId)
                        .header("Authorization", cidadao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.alterar-status").doesNotExist());

        mvc.perform(patch("/api/ocorrencias/" + ocorrenciaId + "/status")
                        .header("Authorization", cidadao)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ATENDIMENTO\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/ocorrencias/" + ocorrenciaId + "/status")
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVIDA"));

        // Verifica as consultas com filtros e JPQL
        mvc.perform(get("/api/pontos-coleta")
                        .param("ativo", "true"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pontos-coleta/busca-nome")
                        .param("nome", "Lixeira"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/pontos-coleta")
                        .param("ativo", "true")
                        .param("termo", "Lixeira"))
                .andExpect(status().isOk());

        // Atualiza o ponto e verifica a invalidação do cache
        mvc.perform(put("/api/pontos-coleta/" + pontoId)
                        .header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ponto Atualizado\",\"descricaoLocalizacao\":\"Hall\",\"capacidadeLitros\":60}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Ponto Atualizado"));

        assertNull(cacheManager.getCache("pontos").get(pontoId));

        // Verifica a desativação e reativação do ponto
        mvc.perform(delete("/api/pontos-coleta/" + pontoId)
                        .header("Authorization", admin))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/pontos-coleta/" + pontoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));

        mvc.perform(patch("/api/pontos-coleta/" + pontoId + "/reativar")
                        .header("Authorization", admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
    }
}
