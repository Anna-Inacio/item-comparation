# Item Comparation — API RESTful para comparar produtos

## Resumo
Esta API fornece endpoints REST básicos para listar produtos, obter detalhes de um produto e comparar produtos entre si. Os dados iniciais são carregados de `src/main/resources/products.json` no startup (via `DataLoader`) e a implementação atual usa uma store em memória (mapa) através do `ProductsService`.

## Objetivo
Criar uma API RESTful simples que retorne detalhes de vários itens a serem comparados e forneça uma rota para comparar produtos por ids.

---

## Arquitetura e design

A aplicação segue um design em camadas simples:

- **Controller (entrada HTTP)**
  - `ProductsController.java` — expõe os endpoints `/products`, `/products/{id}` e `/products/compare`.
  - Controllers são mantidos “finos”: recebem parâmetros HTTP e delegam lógica para o serviço.

- **Service (lógica de negócio)**
  - `ProductsService.java` — contém operações de negócio: listar, buscar por id, salvar, comparar produtos, parsing de CSV quando aplicável.
  - Conversão e validação de regras (ex.: parse CSV, validar ids) foram movidas para o service ou utilidades para manter controllers simples.

- **DTOs / Domain**
  - `Product.java` — modelo de domínio (id, name, imageUrl, description, price, classification, specifications).
  - `CompareResult.java` e `Differences.java` — DTOs para a resposta de comparação.
  - `ErrorResponse.java` — DTO padrão para respostas de erro amigáveis.

- **Exceptions / Error Handling**
  - Exceções customizadas: `ProductNotFoundException`, `InvalidProductIdException`, `DataLoadException`.
  - `GlobalHandlerController.java` (ou `GlobalExceptionHandler`) — `@RestControllerAdvice` que mapeia exceções para `ErrorResponse` JSON e centraliza logging (gera/usa `errorId` para correlação).
  - Tratamento de erros de binding/param: `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException` -> 400.

- **Infra / Repository**
  - `DataLoader.java` — carrega `products.json` no startup e salva via `ProductsService.saveAll`.
  - Store atual: `ConcurrentHashMap<Long, Product>` em `ProductsService` (memória). Pode-se substituir facilmente por JPA/DB no futuro.

---

## Principais endpoints

### 1) Listar produtos
- **GET** `/products`  
- **Resposta**: `200 OK` + array JSON (quando vazio, atualmente retorna `204 No Content` por implementação; recomenda-se retornar `200` com `[]` para consistência).
- **Exemplo:**
```bash
curl -i "http://localhost:8080/products" -H "Accept: application/json"
```

### 2) Obter produto por id
- **GET** `/products/{productId}`  
- **Path variable**: `productId` (Long)  
- **Respostas**:
  - `200 OK` + objeto produto quando existe
  - `404 Not Found` com `ErrorResponse` quando não existe
- **Exemplo:**
```bash
curl -i "http://localhost:8080/products/2" -H "Accept: application/json"
```

### 3) Comparar produtos

Há duas formas/variações usadas no projeto:

- **A)** Query params (comparação de dois ids)  
  - **GET** `/products/compare?left=1&right=2`  
  - Retorna um `CompareResult` com `left`, `right` e `differences`.

- **B)** CSV no path (implementação alternativa)  
  - **GET** `/products/compare/{productIds}` onde `{productIds}` é uma string CSV, ex.: `/products/compare/1,2` ou `/products/compare/1,2,3`  
  - O controller delega para o service o parsing ou o service faz `compareFromCsv`.

- **Exemplos:**
```bash
curl -i "http://localhost:8080/products/compare/1,2" -H "Accept: application/json"
curl -i "http://localhost:8080/products/compare?left=1&right=2" -H "Accept: application/json"
```

---

## Formato das respostas (exemplos)

### Produto (sucesso)
```json
{
  "id": 2,
  "name": "Smartphone X",
  "imageUrl": "https://...",
  "description": "Um ótimo telefone",
  "price": 1299.99,
  "classification": "Eletrônicos",
  "specifications": "..."
}
```

### CompareResult (exemplo)
```json
{
  "left": { "id":1, "name":"Produto A", "price":10.0 },
  "right": { "id":2, "name":"Produto B", "price":12.5 },
  "differences": {
    "priceDifference": 2.5,
    "sameClassification": false,
    "differingFields": ["price", "classification"]
  }
}
```

### Erro padrão (`ErrorResponse`)
```json
{
  "timestamp": "2025-10-25T14:32:10.123",
  "status": 404,
  "error": "Not Found",
  "message": "Produto com ID 5 não encontrado.",
  "path": "/products/5"
}
```
> Observação: o projeto também pode retornar variantes com `errorId`, `code` e `detail` — o handler global pode ser ajustado conforme preferência.

---

## Configuração, build e execução

### Requisitos
- Java 17
- Maven (use o wrapper `./mvnw` fornecido)

### Build
```bash
cd /path/to/project
./mvnw -DskipTests clean package
```
O JAR será gerado em `target/item-comparation-0.0.1-SNAPSHOT.jar`.

### Run (desenvolvimento)
```bash
./mvnw spring-boot:run
```

### Run via JAR
```bash
java -jar target/item-comparation-0.0.1-SNAPSHOT.jar
```

---

## Arquivos de configuração importantes
- `src/main/resources/application.properties` — configurações padrão (porta, H2 console, `server.error.*`)  
  Exemplo de propriedades importantes:
```properties
# transformar 404 sem handler em exceção (útil para handler global)
spring.mvc.throw-exception-if-no-handler-found=true

# controla se o resource handler mapeia recursos estáticos
spring.web.resources.add-mappings=false

# controlar stacktrace no body de erro
server.error.include-stacktrace=never
server.error.include-message=never
```

- `src/main/resources/application-dev.properties` — perfil `dev` (pode habilitar `on_param` e debug)

---

## Dados iniciais
- `src/main/resources/products.json` — arquivo JSON que o `DataLoader` consome no startup e popula a store em memória.

---

## Tratamento de erros e logging
- Exceções customizadas são convertidas para `ErrorResponse` por `GlobalHandlerController` e retornam status apropriado (`400`, `404`, `500`).  
- Boas práticas:
  - Adicionar `errorId` (UUID) no `ErrorResponse` para correlacionar logs do servidor.
  - Logs devem gravar stacktrace e `errorId`; payload deve expor apenas mensagem amigável (ou `detail` condicionado a `dev`/`debug`).
  - Em produção, `server.error.include-stacktrace` deve permanecer `never` para não vazar informações.

---

## Por que manter parsing/validação no *service* (boa prática)
- Controller fica fino e foca em protocolo/HTTP.  
- Service concentra regras de negócio e parsing reutilizável (ex.: `compareFromCsv`, util `CsvIdParser`).  
- Facilita testes unitários no serviço sem dependência de infra HTTP.  
- `ControllerAdvice` cuida do mapeamento de exceções para respostas HTTP.

---

## Pilha tecnológica escolhida (backend)

- **Java 17** — LTS, compatível com bibliotecas modernas.  
- **Spring Boot 3.x** — autoconfiguração, produtividade, ecossistema.  
- **Spring Web (Spring MVC)** — rotas REST, `@RestController`, `@ControllerAdvice`.  
- **Jackson** — serialização/deserialização JSON.  
- **Maven** (`pom.xml` + `./mvnw`) — build e dependências.  
- **SLF4J + Logback** (padrão Spring Boot) — logging.  
- **Embedded Tomcat** — servidor HTTP incorporado.  
- **(opcional)** H2 console para dev (`spring.h2.console.enabled=true`).

Racional: produtividade, comunidade ampla e fácil evolução (ex.: migrar store para JPA).

---

## Testes e qualidade
- Recomendado adicionar:
  - Unit tests para `CsvIdParser` e `ProductsService.compare` (happy path + erros).
  - Integration tests com `MockMvc` para `ProductsController`.
- Executar:
```bash
./mvnw test
```
> Observação: durante o build acima alguns passos foram executados com `-DskipTests`; remova essa flag para rodar os testes.

---

## Boas práticas e próximos passos sugeridos
- Padronizar retorno de listagens: preferir `200 OK` com `[]` em vez de `204 No Content` para endpoints que sempre retornam listas.  
- Adicionar `errorId` no `ErrorResponse` e log com MDC para correlação.  
- Migrar store de memória para persistência (JPA/Hibernate + PostgreSQL) se necessário.  
- Adicionar OpenAPI/Swagger (`springdoc-openapi`) para documentação automática.  
- Adicionar testes unitários e integração.  
- Para listas grandes, aceitar `POST` com JSON no body em vez de `GET` com query string (evita limites de URL).

---

## Troubleshooting (erros comuns e soluções rápidas)

- **`NoResourceFoundException: No static resource products.`**  
  - Causa: Resource handler tentou servir `/products/` como arquivo estático.  
  - Solução: habilitar `spring.mvc.throw-exception-if-no-handler-found=true` e/ou desabilitar `spring.web.resources.add-mappings` e tratar `NoResourceFoundException` no `ControllerAdvice`. Certifique-se que `ProductsController` tenha `@GetMapping({ "", "/" })` se desejar atender `/products/` com barra.

- **`Required URI template variable 'productIds' for method parameter type List is not present`**  
  - Causa: tentativa de binding direto para `List<Long>` via `@PathVariable`.  
  - Solução: receber a variável como `String` (CSV) e converter para `List<Long>` no service/utility, ou usar `@RequestParam List<Long> ids`.

- **IDs inválidos ou mal formatados**  
  - Solução: validar no service e lançar `InvalidProductIdException`, tratado por `ControllerAdvice` para retornar `400`.

---

## Exemplos de uso rápido

- Listar:
```bash
curl -s "http://localhost:8080/products" -H "Accept: application/json" | jq
```

- Pegar por id:
```bash
curl -s "http://localhost:8080/products/2" -H "Accept: application/json" | jq
```

- Comparar por CSV in-path:
```bash
curl -s "http://localhost:8080/products/compare/1,2" -H "Accept: application/json" | jq
```

- Comparar por query params:
```bash
curl -s "http://localhost:8080/products/compare?left=1&right=2" -H "Accept: application/json" | jq
```

---

## Onde olhar no código (principais arquivos)

- `src/main/java/com/example/item_comparation/controller/ProductsController.java`
- `src/main/java/com/example/item_comparation/service/ProductsService.java`
- `src/main/java/com/example/item_comparation/repository/DataLoader.java`
- `src/main/java/com/example/item_comparation/domain/Product.java`
- `src/main/java/com/example/item_comparation/dto/CompareResult.java`
- `src/main/java/com/example/item_comparation/dto/Differences.java`
- `src/main/java/com/example/item_comparation/exception/*` (handler e exceptions)
- `src/main/resources/products.json`
- `pom.xml`

---

## Licença e contribuição
Inclua aqui a licença desejada (ex.: MIT) e instruções rápidas de como abrir PRs e rodar testes localmente.

---

## Contato / Suporte
Posso aplicar automaticamente as seguintes alterações, se você quiser:
- Alterar `/products` para sempre retornar `200` com `[]` em vez de `204`.  
- Extrair parsing CSV para `CsvIdParser` e adicionar unit tests.  
- Adicionar `errorId` + MDC no `GlobalHandlerController` e ajustar `ErrorResponse`.

Diga qual opção quer que eu implemente primeiro e eu faço as edições e executo build/testes automaticamente.

