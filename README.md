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

- **Domain**
  - `Product.java` — modelo de domínio (id, name, imageUrl, description, price, classification, specifications).

- **Exceptions / Error Handling**
  - Exceções customizadas: `ProductNotFoundException`, `DataLoadException`.
  - `GlobalHandlerController.java` (ou `GlobalExceptionHandler`) — `@RestControllerAdvice` que mapeia exceções para `ErrorResponse` JSON.

- **Infra / Repository**
  - `DataLoader.java` — carrega `products.json` no startup e salva via `ProductsService.saveAll`.
  - Store atual: `ConcurrentHashMap<Long, Product>` em `ProductsService` (memória). Pode-se substituir facilmente por JPA/DB no futuro.

---

## Principais endpoints

### 1) Listar produtos
- **GET** `/products`  
- **Resposta**: `200 OK` + array JSON (quando vazio, retorna `200` com `[]` para consistência).
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

- CSV no path
  - **GET** `/products/compare/{productIds}` onde `{productIds}` é uma string CSV, ex.: `/products/compare/1,2` ou `/products/compare/1,2,3`  
  - O controller delega para o service o parsing ou o service faz `compareFromCsv`.

- **Exemplos:**
```bash
curl -i "http://localhost:8080/products/compare/1,2" -H "Accept: application/json"
```

---

## Formato das respostas (exemplos)

### Produto (sucesso)
```json
{
  "id": 1,
  "name": "Notebook Gamer",
  "imageUrl": "https://example.com/images/notebook-gamer.jpg",
  "description": "Notebook Gamer com processador Intel i7, 16GB RAM",
  "price": 5500.50,
  "classification": "Eletrônicos",
  "specifications": "Processador: Intel i7, RAM: 16GB"
}
```

### CompareResult (exemplo)
```json
[
  {
    "id": 2,
    "name": "Mouse Sem Fio",
    "imageUrl": "https://example.com/images/mouse-sem-fio.jpg",
    "description": null,
    "price": 99.99,
    "classification": "Eletrônicos",
    "specifications": "Tipo: Óptico, Conexão: Bluetooth"
  },
  {
    "id": 3,
    "name": "Monitor Ultra HD",
    "imageUrl": "https://example.com/images/monitor-ultra-hd.jpg",
    "description": "Monitor Ultra HD 4K de 27 polegadas",
    "price": 2200.00,
    "classification": "Eletrônicos",
    "specifications": "Resolução: 3840x2160, Tamanho: 27 polegadas"
  }
]
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

## Dados iniciais
- `src/main/resources/products.json` — arquivo JSON que o `DataLoader` consome no startup e popula a store em memória.

---

## Tratamento de erros e logging
- Exceções customizadas são convertidas para `ErrorResponse` por `GlobalHandlerController` e retornam status apropriado (`400`, `404`, `500`).

---

## Por que manter parsing/validação no *service*
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
- **Embedded Tomcat** — servidor HTTP incorporado.  

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

## Troubleshooting (erros comuns e soluções rápidas)

- **`Required URI template variable 'productIds' for method parameter type List is not present`**  
  - Causa: tentativa de binding direto para `List<Long>` via `@PathVariable`.  
  - Solução: receber a variável como `String` (CSV) e converter para `List<Long>` no service/utility, ou usar `@RequestParam List<Long> ids`.

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

---

## Onde olhar no código (principais arquivos)

- `src/main/java/com/example/item_comparation/controller/ProductsController.java`
- `src/main/java/com/example/item_comparation/service/ProductsService.java`
- `src/main/java/com/example/item_comparation/repository/DataLoader.java`
- `src/main/java/com/example/item_comparation/domain/Product.java`
- `src/main/java/com/example/item_comparation/exception/*` (handler e exceptions)
- `src/main/resources/products.json`
- `pom.xml`

---
# Diagrama

<img width="1432" height="527" alt="DesafioMeli drawio" src="https://github.com/user-attachments/assets/b39cbafd-caba-4a38-9d34-97916c0be9a5" />
