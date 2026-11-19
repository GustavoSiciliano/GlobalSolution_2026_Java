# Binova - Java Advanced | FIAP Global Solution 2026/2

**Tema:** CitizenTech  
**Disciplina:** Java Advanced  
**Turma:** 2TDSPS  
**Repositório:** https://github.com/GustavoSiciliano/GlobalSolution_2026_Java  
**Vídeo demonstrativo de Java:** https://youtu.be/2SlwoG4p7Oc  
**Mobile integrado:** https://github.com/Mpgouveia-n/Binova---Application-Mobile-Development-  
**Protótipo mobile:** https://www.figma.com/design/qxJAkVzKzr1JdKEHtsQDuU

> **Como avaliar:** siga a [instalação do Java](#6-configuração-e-execução), o [roteiro pelo Swagger](#8-roteiro-de-avaliação-pelo-swagger) e a [integração Java e Mobile](#13-execução-integrada-com-o-mobile-em-dart).

## 1. Apresentação do projeto

O **Binova** é uma proposta CitizenTech para monitoramento de pontos de coleta de resíduos. Cidadãos podem consultar a situação das lixeiras e registrar ocorrências; a aplicação também recebe medições de sensores e ajuda a identificar pontos que precisam de atenção.

Nesta disciplina, foi desenvolvida uma API REST com **Java 21 e Spring Boot 4.1.2**. A API oferece autenticação JWT, autorização por papel, consultas com Spring Data JPA, paginação, tratamento de erros, cache, respostas HATEOAS e monitoramento pelo Actuator. Para facilitar a execução local, utiliza **H2 persistente em arquivo**. Não é necessário ter um sensor físico: as leituras podem ser registradas pela própria API.

O projeto Java também pode ser utilizado pelo **Binova Mobile em Dart Console**, por meio de requisições HTTP com JSON. A API centraliza autenticação, permissões, consultas, classificação de preenchimento e registro das ocorrências; o Mobile apresenta essas operações ao cidadão. O protótipo visual no Figma apresenta a proposta de interface para celular e é independente da execução em Dart Console.

## 2. Integrantes

| Integrante | RM |
|---|---|
| Gustavo de Jesus | 567926 |
| Gustavo Siciliano | 568419 |
| Samuel Keniti | 567614 |
| Manuel Pedro | 567458 |
| Vinicius Morrone | 566884 |

**Instituição:** FIAP  
**Curso:** Análise e Desenvolvimento de Sistemas  
**Turma:** 2TDSPS

## 3. Tecnologias utilizadas

| Tecnologia | Uso no Binova |
|---|---|
| Java 21, Spring Boot e Spring Web MVC | Construção da API REST |
| Spring Data JPA, Hibernate e H2 | Persistência, relacionamentos e consultas |
| Bean Validation e `@RestControllerAdvice` | Validações e erros JSON |
| Spring Security, JWT e BCrypt | Login, permissões e proteção de senhas |
| Spring Cache | Cache das consultas de detalhes dos pontos |
| Spring HATEOAS | Links para os recursos e ações relacionadas |
| Spring Boot Actuator | `health`, `info` e `metrics` |
| Swagger / OpenAPI | Documentação e execução interativa dos endpoints |
| JUnit 5 e testes Spring | Testes automatizados da aplicação |

### Estrutura principal

```text
GlobalSolution_2026_Java/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
├── Diagram/
│   └── Diagrama.png
└── src/
    ├── main/
    │   ├── java/br/com/fiap/binova/
    │   │   ├── api/
    │   │   ├── config/
    │   │   ├── domain/
    │   │   ├── dto/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   ├── service/
    │   │   └── specification/
    │   └── resources/application.yaml
    └── test/
        ├── java/br/com/fiap/binova/
        └── resources/
```

## 4. Modelo de dados e relacionamentos JPA

As quatro entidades persistidas são `Usuario`, `PontoColeta`, `Ocorrencia` e `LeituraSensor`.

![Diagrama de entidades JPA do Binova](Diagram/Diagrama.png)

| Relacionamento | Cardinalidade | Implementação JPA |
|---|---|---|
| `Usuario` → `Ocorrencia` | 1:N | `@OneToMany(mappedBy = "usuario")` / `@ManyToOne` |
| `PontoColeta` → `Ocorrencia` | 1:N | `@OneToMany(mappedBy = "pontoColeta")` / `@ManyToOne` |
| `PontoColeta` → `LeituraSensor` | 1:N | `@OneToMany(mappedBy = "pontoColeta")` / `@ManyToOne` |

As entidades filhas utilizam `@ManyToOne(fetch = FetchType.LAZY, optional = false)` com `@JoinColumn`. O `LAZY` evita carregar imediatamente as entidades relacionadas; a camada de serviços controla o acesso aos dados. **Não existe `cascade` explícito** nessas associações. Os IDs utilizam `GenerationType.IDENTITY`; os valores de medição são `BigDecimal`, as datas `OffsetDateTime` e os campos de papel/tipo/status são enums persistidos como texto.

O ponto de coleta possui **exclusão lógica**: `DELETE` define `ativo = false` e preserva o histórico de leituras e ocorrências.

## 5. Banco de dados e contas de teste

O arquivo `src/main/resources/application.yaml` configura o banco H2 local:

| Configuração | Valor |
|---|---|
| URL JDBC | `jdbc:h2:file:./data/binova;DB_CLOSE_ON_EXIT=FALSE` |
| Usuário | `sa` |
| Senha | Em branco |
| Console | http://localhost:8080/h2-console |
| Tabelas | Gerenciadas pelo Hibernate com `ddl-auto: update` |

Os dados ficam em `data/binova.mv.db` e persistem entre reinicializações.

### Contas de demonstração

| Papel | E-mail | Senha de demonstração |
|---|---|---|
| `ADMIN` | `admin@binova.local` | `AdminLocal2026` |
| `CIDADAO` | `cidadao@binova.local` | `CidadaoLocal2026` |

Esses são **exemplos para uma execução local**, criados somente quando a opção de demonstração está habilitada e as senhas são fornecidas.

## 6. Configuração e execução

### 6.1 Pré-requisitos

- JDK **21** instalado;
- PowerShell no Windows (ou terminal compatível no Linux/macOS);
- Conexão à internet na primeira execução para baixar dependências Maven;
- Projeto aberto na pasta que contém `pom.xml`.

Clone o repositório (ou extraia o ZIP e abra sua pasta):

```powershell
git clone https://github.com/GustavoSiciliano/GlobalSolution_2026_Java.git
cd .\GlobalSolution_2026_Java
java -version
.\mvnw.cmd -version
```

### 6.2 Preparar os perfis de demonstração

No **mesmo PowerShell** em que você iniciará o Spring Boot:

```powershell
$env:BINOVA_DEMO = "true"
$env:APP_DEMO_ADMIN_PASSWORD = "AdminLocal2026"
$env:APP_DEMO_CIDADAO_PASSWORD = "CidadaoLocal2026"
```

A configuração real utiliza `app.demo.enabled` (variável `BINOVA_DEMO`) e as senhas de ambiente `APP_DEMO_ADMIN_PASSWORD` e `APP_DEMO_CIDADAO_PASSWORD`. Para não criar as contas de exemplo, utilize `BINOVA_DEMO=false` - nesse caso, o roteiro abaixo exigirá contas cadastradas por outro meio.

O JWT usa uma chave temporária em desenvolvimento quando `JWT_SECRET_BASE64` não está definida. Tokens antigos deixam de valer ao reiniciar a aplicação. Se configurar uma chave própria, mantenha o segredo fora do GitHub e utilize Base64 de pelo menos 32 bytes.

### 6.3 Compilar e iniciar

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

O primeiro comando compila e executa os testes automatizados; o segundo inicia a aplicação. Mantenha o terminal da API aberto. Nas próximas inicializações, não é necessário repetir `clean verify` se o código não mudou; basta executar `.\mvnw.cmd spring-boot:run` no terminal do Java.

| Recurso | Endereço local |
|---|---|
| **Swagger UI - avaliação da API** | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |
| Actuator Health | http://localhost:8080/actuator/health |
| Actuator Info | http://localhost:8080/actuator/info |

### 6.4 Conferir as tabelas pelo H2

Com a API iniciada, abra http://localhost:8080/h2-console e use a URL `jdbc:h2:file:./data/binova`, usuário `sa` e senha vazia. Execute:

```sql
SELECT * FROM USUARIO;
SELECT * FROM PONTO_COLETA;
SELECT * FROM LEITURA_SENSOR;
SELECT * FROM OCORRENCIA;
```

O console H2 integrado à aplicação é preferível a abrir outra instância externa sobre o mesmo arquivo do banco.

## 7. Endpoints principais

| Método | Rota | Acesso | Resposta de sucesso |
|---|---|---|---|
| `POST` | `/api/auth/cadastro` | Público | 201 |
| `POST` | `/api/auth/login` | Público | 200 + JWT |
| `GET` | `/api/usuarios/me` | Autenticado | 200 |
| `GET` | `/api/pontos-coleta` | Público | 200 |
| `GET` | `/api/pontos-coleta/busca-nome?nome=...` | Público | 200 |
| `GET` | `/api/pontos-coleta/{id}` | Público | 200 |
| `GET` | `/api/pontos-coleta/{id}/situacao` | Público | 200 |
| `POST` | `/api/pontos-coleta` | ADMIN | 201 |
| `PUT` | `/api/pontos-coleta/{id}` | ADMIN | **200 + JSON** |
| `DELETE` | `/api/pontos-coleta/{id}` | ADMIN | **204** (desativação lógica) |
| `PATCH` | `/api/pontos-coleta/{id}/reativar` | ADMIN | 200 |
| `GET` | `/api/pontos-coleta/{pontoId}/leituras` | Autenticado | 200 |
| `GET` | `/api/pontos-coleta/{pontoId}/leituras/{id}` | Autenticado | 200 |
| `POST` | `/api/pontos-coleta/{pontoId}/leituras` | ADMIN | 201 |
| `GET`, `POST` | `/api/ocorrencias` | Autenticado | 200 / 201 |
| `GET`, `PUT`, `DELETE` | `/api/ocorrencias/{id}` | Autenticado, conforme autoria e regras | 200 / 204 |
| `PATCH` | `/api/ocorrencias/{id}/status` | ADMIN | 200 |
| `GET` | `/actuator/health`, `/actuator/info` | Público | 200 |
| `GET` | `/actuator/metrics` | ADMIN | 200 |

As listagens utilizam `page`, `size` e `sort`; pontos aceitam `termo` e `ativo`, ocorrências aceitam `status`.

## 8. Roteiro de avaliação pelo Swagger

Acesse **http://localhost:8080/swagger-ui.html**. Em cada endpoint: abra a operação, clique em **Try it out**, preencha o JSON, clique em **Execute** e confira **Response code** e **Response body**. Siga a sequência abaixo. **Use os IDs retornados pelo próprio Swagger, sem pressupor registros anteriores.**

### 8.1 Autenticação JWT (ADMIN)

Abra `POST /api/auth/login` e envie:

```json
{
  "email": "admin@binova.local",
  "senha": "AdminLocal2026"
}
```

**Esperado:** HTTP **200** e objeto com `token`, `tipo`, `expiraEmSegundos` e `papel`. Copie **somente o valor de `token`** e clique em **Authorize** no Swagger para inserir o JWT no esquema `bearerAuth`. A autorização deverá ser enviada como `Authorization: Bearer <token>`.

Se a conta já existia no H2 com outra senha, use a senha anterior ou configure um banco novo **exclusivo para testes**, sem apagar dados importantes.

### 8.2 POST - cadastrar ponto de coleta

Abra `POST /api/pontos-coleta` com o perfil ADMIN autorizado:

```json
{
  "nome": "Ecoponto Avaliacao Java",
  "descricaoLocalizacao": "Entrada da biblioteca",
  "capacidadeLitros": 80
}
```

**Esperado:** HTTP **201 Created**, retorno do ponto e cabeçalho `Location` com `/api/pontos-coleta/{id}`. **Anote o `id` criado** para os próximos testes.

### 8.3 GET - consultar ponto, filtros e HATEOAS

Execute no Swagger:

1. `GET /api/pontos-coleta/{id}` com o ID recém-criado → **200**, incluindo os dados e os links `_links` como `self`, `situacao` e `leituras`;
2. `GET /api/pontos-coleta` → **200**, com paginação; pode testar `page=0`, `size=10`, `sort=id,asc`;
3. `GET /api/pontos-coleta?ativo=true` → **200**, filtrando os ativos;
4. `GET /api/pontos-coleta/busca-nome?nome=Ecoponto` → **200**, para demonstrar a consulta JPQL.

As respostas de detalhes são cacheadas pelo serviço; o cache é invalidado nas operações de alteração.

### 8.4 PUT - atualizar ponto de coleta

Abra `PUT /api/pontos-coleta/{id}` com o **mesmo ID**, e envie:

```json
{
  "nome": "Ecoponto Java Atualizado",
  "descricaoLocalizacao": "Entrada principal - Bloco B",
  "capacidadeLitros": 120
}
```

**Esperado:** HTTP **200 OK** com o registro atualizado. Repita `GET /api/pontos-coleta/{id}` para confirmar os novos valores. O `GET` após a edição também demonstra que o cache não manteve os dados antigos.

### 8.5 DELETE - desativação lógica

Abra `DELETE /api/pontos-coleta/{id}` com o mesmo ID e execute como ADMIN.

**Esperado:** HTTP **204 No Content**. Faça `GET /api/pontos-coleta/{id}`: o ponto ainda existe, mas `ativo` deve estar como `false`. Isso preserva leituras e ocorrências.

Para continuar a demonstração do sensor, execute `PATCH /api/pontos-coleta/{id}/reativar` como ADMIN (**200**).

### 8.6 Tratamento de erros - 400 e 404

**POST inválido - 400 Bad Request.** Abra `POST /api/pontos-coleta` como ADMIN e envie:

```json
{
  "nome": "",
  "descricaoLocalizacao": "",
  "capacidadeLitros": 0
}
```

**Esperado:** HTTP **400** com erro padronizado em JSON e campos inválidos.

**GET inexistente - 404 Not Found.** Abra `GET /api/pontos-coleta/{id}` com `999999999`.

**Esperado:** HTTP **404** em JSON, com a mensagem de recurso não encontrado.

### 8.7 Segurança por papéis - 403

No Swagger, utilize **Authorize** com um novo token do perfil CIDADAO. Para obtê-lo, execute `POST /api/auth/login`:

```json
{
  "email": "cidadao@binova.local",
  "senha": "CidadaoLocal2026"
}
```

Com o token do cidadão, tente `POST /api/pontos-coleta` usando o JSON válido do passo 8.2.

**Esperado:** HTTP **403 Forbidden**. A rota de cadastro de pontos é permitida apenas para ADMIN. Depois, autorize-se novamente com o token de ADMIN para os testes administrativos restantes.

### 8.8 Leitura de sensor e situação do ponto

Utilize o mesmo ponto já **reativado** e a autenticação ADMIN. Abra `POST /api/pontos-coleta/{pontoId}/leituras`, preencha o ID e envie:

```json
{
  "distanciaCm": 12,
  "nivelPercentual": 85
}
```

**Esperado:** HTTP **201 Created**. Em seguida, execute `GET /api/pontos-coleta/{id}/situacao` (**200**). Uma leitura válida e recente de 85% deve produzir `PROXIMA_DO_LIMITE`. Após **15 minutos**, a mesma leitura poderá resultar em `SEM_DADOS_ATUALIZADOS`. Com usuário autenticado, consulte também o histórico pelo endpoint `GET /api/pontos-coleta/{pontoId}/leituras`.

### 8.9 Ocorrências registradas por cidadãos

Autorize o perfil CIDADAO e execute `POST /api/ocorrencias`:

```json
{
  "pontoColetaId": 1,
  "tipo": "LIXEIRA_CHEIA",
  "descricao": "Lixeira cheia na entrada da biblioteca"
}
```

**Substitua `1` pelo ID do ponto reativado.** O usuário da ocorrência é definido pelo JWT; não é enviado no JSON. **Esperado:** HTTP **201 Created**.

Com o perfil ADMIN, utilize `PATCH /api/ocorrencias/{id}/status`:

```json
{
  "status": "EM_ATENDIMENTO"
}
```

**Esperado:** HTTP **200**, demonstrando a alteração do atendimento.

### 8.10 Actuator e monitoramento

Acesse no navegador ou pelo Swagger, quando disponíveis na lista de endpoints:

- http://localhost:8080/actuator/health - **200**, saúde da API;
- http://localhost:8080/actuator/info - **200**, identificação da aplicação;
- http://localhost:8080/actuator/metrics - **200 apenas com JWT ADMIN**; fora do Swagger, use um cliente HTTP que permita enviar o cabeçalho `Authorization: Bearer <token>`.

A rota `metrics` está protegida. Não é necessário tornar esse endpoint público para a demonstração.

## 9. Decisões técnicas e justificativas

### 9.1 Consultas JPA

- **Método derivado:** `PontoColetaRepository.findByAtivoTrue(Pageable)` lista pontos ativos com paginação; `LeituraSensorRepository.findFirstByPontoColetaIdOrderByRegistradoEmDesc` recupera a medição mais recente. São consultas simples expressas pelo nome do método.
- **JPQL (`@Query`):** `PontoColetaRepository.buscarPorNome` faz busca textual com `lower(...)` e paginação, útil para localizar lixeiras por nome sem diferenciar maiúsculas e minúsculas.
- **Specification:** `PontoSpecifications.filtros` combina dinamicamente os filtros opcionais de texto e atividade. Isso evita diversos métodos específicos para cada combinação.
- **Paginação e ordenação:** `Pageable` reduz o volume de dados das listagens e permite definir `page`, `size` e `sort`.

### 9.2 Cache

`PontoColetaService.consultar(Long id)` utiliza `@Cacheable(value = "pontos", key = "#id")` na consulta de detalhes de cada ponto. `criar`, `atualizar`, `desativar` e `reativar` utilizam `@CacheEvict(value = "pontos", allEntries = true)` para invalidar a informação depois de alterações. O cache configurado é **simples e em memória**.

### 9.3 Segurança JWT e CORS

O login valida e-mail e senha (BCrypt), emite JWT e permite acessar endpoints protegidos por papel. O perfil `ADMIN` pode criar e administrar pontos e lançar leituras; o `CIDADAO` pode registrar ocorrências. A verificação de permissões ocorre no Spring Security e nos serviços, não apenas na interface do Swagger. O CORS permite, por padrão, `http://localhost:3000`, `http://localhost:5173` e `http://localhost:8081`, configuráveis por `APP_CORS_ALLOWED_ORIGINS`.

### 9.4 HATEOAS

Os detalhes dos pontos e os recursos de ocorrências usam `EntityModel`, incluindo links `self` e para recursos relacionados. A presença de links administrativos depende do perfil e do estado do recurso; as autorizações reais continuam sendo aplicadas pela segurança.

### 9.5 Regras de negócio

Uma leitura válida e recente define a situação do ponto: abaixo de 50% = `DISPONIVEL`; de 50% até menos de 80% = `ATENCAO`; de 80% a 100% = `PROXIMA_DO_LIMITE`. Na ausência de leitura, após 15 minutos ou com dados inválidos, a situação é `SEM_DADOS_ATUALIZADOS`.

## 10. Testes automatizados

O código contém testes Spring/JUnit para inicialização do contexto, JWT, cálculo de situação e fluxo de API com autenticação, permissões, pontos, leituras, ocorrências e demais recursos. Execute na raiz do repositório:

```powershell
.\mvnw.cmd clean verify
```

## 11. Problemas comuns e correções

| Sintoma | Verificação e solução |
|---|---|
| Java ou Maven não encontrado | Confira `java -version` (JDK 21). Use `mvnw.cmd`, não é necessário instalar Maven globalmente. |
| API não inicia: faltam senhas de demonstração | No mesmo terminal, defina `BINOVA_DEMO=true`, `APP_DEMO_ADMIN_PASSWORD` e `APP_DEMO_CIDADAO_PASSWORD`; depois inicie de novo. |
| Login do exemplo retorna 401 | Confira o perfil, a senha e se já existem contas antigas no H2; reiniciar não altera senhas antigas. |
| Swagger exibe 401 em operação protegida | Faça login e clique em **Authorize**; informe um token válido. |
| Swagger exibe 403 no POST de pontos | É necessário utilizar JWT com papel `ADMIN`, não `CIDADAO`. |
| Token deixa de funcionar após reinício | Gere um novo token: a chave padrão do JWT é temporária em desenvolvimento. |
| Porta 8080 em uso | Encerre a outra API ou defina `$env:SERVER_PORT="8081"` antes de iniciar; ajuste as URLs dos testes. |
| HTTP 404 / 405 | Verifique o ID e o método; a leitura usa `GET` e `POST` em rota aninhada, não `DELETE`. |
| H2 ocupado / arquivo em uso | Não inicie duas instâncias usando o mesmo arquivo H2; use o console da aplicação iniciada. |
| Necessidade de dados limpos para avaliar | Em um ambiente de testes, use um novo arquivo H2 configurado via `SPRING_DATASOURCE_URL`; não apague dados existentes sem confirmação. |

Para conferir a porta no Windows:

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue |
    Select-Object LocalPort, OwningProcess
```

Se for necessário usar outra porta, faça antes de iniciar:

```powershell
$env:SERVER_PORT = "8081"
.\mvnw.cmd spring-boot:run
```

Ao iniciar novamente em outro PowerShell, configure também as variáveis de demonstração nesse terminal.

## 12. ODS e impacto do projeto

O Binova se relaciona com dois Objetivos de Desenvolvimento Sustentável (ODS) da Agenda 2030 da ONU:

| ODS | Relação com o Binova |
|---|---|
| **ODS 11: Cidades e Comunidades Sustentáveis** | Contribui para o acompanhamento de pontos de coleta e a identificação de problemas em espaços urbanos, apoiando a organização dos serviços e a participação dos moradores. |
| **ODS 12: Consumo e Produção Responsáveis** | Incentiva a atenção ao descarte de resíduos, permitindo identificar recipientes próximos da capacidade e situações de descarte inadequado. |

Os cidadãos que utilizam os pontos de coleta são os principais beneficiados. Os responsáveis pela gestão urbana também podem utilizar as informações para acompanhar ocorrências e priorizar ações. O projeto demonstra essas possibilidades por meio da API e de um fluxo de participação cidadã; não representa, por si só, uma medição de impacto real em campo.

## 13. Execução integrada com o Mobile em Dart

**Repositório Mobile:** https://github.com/Mpgouveia-n/Binova---Application-Mobile-Development-

O projeto de Mobile possui uma versão independente em Dart Console, com dados em memória, e uma execução separada preparada para consumir a API Java. Para demonstrar a comunicação HTTP, utilize **a execução integrada**, não o `bin/main.dart` da versão local.

### 13.1. Iniciar a API Java (terminal 1)

Clone e entre no repositório Java ou abra a pasta já baixada. No PowerShell, no diretório com `pom.xml`:

```powershell
$env:BINOVA_DEMO = "true"
$env:APP_DEMO_ADMIN_PASSWORD = "AdminLocal2026"
$env:APP_DEMO_CIDADAO_PASSWORD = "CidadaoLocal2026"

.\mvnw.cmd spring-boot:run
```

Aguarde a inicialização completa e confirme que o processo continua ativo, sem `Application run failed`. A API atende em `http://localhost:8080`. Para conferir, abra:

- Swagger UI: http://localhost:8080/swagger-ui.html
- Saúde da API: http://localhost:8080/actuator/health

Se for a primeira execução, os dados de usuário de demonstração são configurados pelas variáveis acima. As informações persistidas no H2 não são apagadas a cada reinício; senhas antigas permanecem se os usuários já estiverem cadastrados.

### 13.2. Preparar um ponto de coleta no Java

A listagem integrada consulta os dados armazenados no **H2 do Java** e não utiliza os pontos simulados do Mobile original. Em uma base nova, a lista pode estar vazia até que um ponto seja cadastrado.

1. No Swagger, abra `POST /api/auth/login`, informe `admin@binova.local` e `AdminLocal2026` e execute.
2. Copie o valor do campo `token` retornado e utilize o botão **Authorize** para autorizar o esquema JWT `bearerAuth`.
3. Execute `POST /api/pontos-coleta` com o JSON abaixo:

```json
{
  "nome": "Ecoponto Binova Integracao",
  "descricaoLocalizacao": "Entrada principal do bairro",
  "capacidadeLitros": 100
}
```

4. Confira a resposta **201** e guarde o `id` devolvido pela API.
5. Execute `GET /api/pontos-coleta?ativo=true` e confirme que o ponto aparece na propriedade `content` da resposta paginada.

Apenas o perfil `ADMIN` pode cadastrar pontos. Para demonstrar o nível de preenchimento, utilize também `POST /api/pontos-coleta/{pontoId}/leituras` com token de administrador. O exemplo de leitura e sua classificação estão na seção 8.8. A leitura deve ser recente, pois a situação considera uma janela de 15 minutos.

### 13.3. Executar o Mobile integrado (terminal 2)

Abra outro PowerShell, mantendo o Java ativo no primeiro terminal:

```powershell
git clone https://github.com/Mpgouveia-n/Binova---Application-Mobile-Development-.git
cd .\Binova---Application-Mobile-Development-
dart --version
dart pub get
dart run lib/api/bin/main_api.dart
```

Se já clonou o Mobile, abra diretamente a pasta que contém o `pubspec.yaml` e execute os três últimos comandos. O pacote utiliza Dart com SDK compatível com `^3.13.1`.

**Estrutura da integração no Mobile:**

```text
lib/api/
├── api_config.dart
├── api_client.dart
├── bin/
│   └── main_api.dart
├── console/
│   └── binova_console_api.dart
└── services/
    ├── auth_api_service.dart
    ├── ponto_coleta_api_service.dart
    └── ocorrencia_api_service.dart
```

O arquivo `lib/api/api_config.dart` deve apontar para o mesmo servidor em que o Java está em execução:

```dart
static const String baseUrl = 'http://localhost:8080';
```

Esse endereço funciona quando Java e Dart Console estão **no mesmo computador**. Caso execute a API em outra máquina ou porta, configure `baseUrl` com o endereço e a porta corretos. Como a comunicação é feita pelo Dart Console, não é necessário configurar um navegador ou um emulador para esse fluxo.

### 13.4. Fazer login e consultar os pontos pelo Mobile

No menu inicial do Mobile integrado, selecione **Entrar com e-mail e senha** e informe:

```text
E-mail: cidadao@binova.local
Senha: CidadaoLocal2026
```

O Dart envia `POST /api/auth/login`, recebe o JWT e utiliza o token nas chamadas protegidas. Após a identificação, execute esta sequência:

1. **Listar pontos de coleta ativos** para consultar `GET /api/pontos-coleta?ativo=true`.
2. **Consultar ponto por ID e situação** para chamar `GET /api/pontos-coleta/{id}` e `GET /api/pontos-coleta/{id}/situacao`.
3. **Pesquisar pontos por nome** e informe `Ecoponto`, para usar a busca da API Java.
4. **Painel geral dos pontos** para reunir a situação dos pontos ativos consultados.

Se a listagem não trouxer resultados, verifique primeiro o cadastro do ponto no Java e se ele está ativo. Uma resposta vazia não indica necessariamente falha na conexão.

### 13.5. Registrar uma ocorrência e confirmar no Swagger

No Mobile integrado, com o cidadão autenticado:

1. Escolha **Registrar ocorrência**.
2. Informe o `id` de um ponto ativo criado na etapa 13.2.
3. Selecione **Lixeira cheia** e escreva uma descrição.
4. Confirme o cadastro. O Dart envia `POST /api/ocorrencias` com `pontoColetaId`, `tipo` e `descricao`, e o Java identifica o cidadão pelo JWT.
5. Acesse **Minhas ocorrências** no Mobile para consultar o registro retornado pela API.

Para conferir a mesma informação diretamente no Java, no Swagger, faça login como `cidadao@binova.local`, autorize o JWT desse cidadão e execute `GET /api/ocorrencias`. Confira o mesmo ID e a descrição registrada no Mobile. Com outro cidadão, a listagem poderá ser diferente devido à regra de acesso por autoria.

**Fluxo demonstrado:** `Mobile Dart Console → HTTP/JSON → Spring Boot → H2 → resposta HTTP → Mobile`. O processamento e a persistência ficam no Java; o Dart apresenta as informações e recolhe os dados fornecidos pelo cidadão.

### 13.6. Problemas comuns na integração

| Situação | Como resolver |
|---|---|
| Mobile não conecta ao servidor | Confirme que o Java continua executando e que `api_config.dart` usa o mesmo endereço e porta. |
| `Connection refused` ou falha de conexão | Abra `http://localhost:8080/actuator/health` no computador em que o Mobile está sendo executado. |
| Login retorna 401 | Confirme e-mail, senha e a configuração das contas no H2. Usuários já existentes podem ter senha anterior. |
| Operação retorna 403 | Confira se o JWT é de `ADMIN` ou `CIDADAO`, conforme a permissão do endpoint. |
| Lista de pontos vazia | Cadastre um ponto pelo Swagger como administrador e confirme que `ativo` é `true`. |
| Consulta de situação mostra `SEM_DADOS_ATUALIZADOS` | Registre uma leitura válida e recente pelo endpoint de leituras, usando o perfil administrador. |
| JWT deixa de funcionar | Faça login novamente, especialmente após reiniciar o Java com uma nova chave temporária. |
| `main_api.dart` não encontrado | Confira se a execução integrada está em `lib/api/bin/main_api.dart` na versão local do Mobile. |
| Erro de importação no Dart | Execute `dart pub get` na raiz do projeto Mobile e confira os caminhos da pasta `lib/api/`. |
| Porta Java diferente de 8080 | Ajuste a configuração `baseUrl` no Mobile para a porta utilizada pelo Spring Boot. |

## 14. Vídeo demonstrativo e protótipo

**Vídeo de Java Advanced:** https://youtu.be/2SlwoG4p7Oc

O vídeo apresenta a proposta Binova, a estrutura da API Java Spring Boot, as principais funcionalidades no Swagger e o fluxo de integração com o Mobile em Dart Console. Os passos das seções 8 e 13 permitem reproduzir a demonstração localmente.

**Protótipo visual do Mobile:** https://www.figma.com/design/qxJAkVzKzr1JdKEHtsQDuU

O Figma apresenta a proposta de interface para celular, incluindo início, pontos de coleta, detalhes, registro de ocorrências e acompanhamento pelo cidadão. A comunicação HTTP descrita neste README é executada pelo **Dart Console integrado**, e não pelo protótipo visual.

