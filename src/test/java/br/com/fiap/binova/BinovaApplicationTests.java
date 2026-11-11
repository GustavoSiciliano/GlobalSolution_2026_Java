package br.com.fiap.binova;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BinovaApplicationTests {

	// Confirma que a aplicação inicializa sem erros de configuração.
	@Test
	void contextLoads() {
	}

}
