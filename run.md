# Como executar este projeto (item-comparation)

Resumo rápido
- Este documento descreve como compilar, testar e rodar a aplicação localmente, além de exemplos de endpoints e dicas de solução de problemas.

Checklist (o que eu cobri aqui)
- [x] Pré-requisitos (Java, Maven)
- [x] Build da aplicação
- [x] Rodar testes
- [x] Rodar a aplicação em desenvolvimento
- [x] Gerar e executar JAR
- [x] Endpoints disponíveis e exemplos de curl
- [x] Troubleshooting comum

Contexto do projeto
- Spring Boot 3.5.7 (definido em `pom.xml`).
- Java alvo: 17 (ver propriedade `<java.version>` no `pom.xml`).
- O projeto contém um `mvnw` (Maven Wrapper) — prefira usar `./mvnw` para consistência.
- Dados iniciais: `src/main/resources/products.json` é carregado na inicialização por `DataLoader`.

Pré-requisitos
- Java 17 (JDK) instalado e disponível no PATH.
- Git (opcional).
- Rede/porta livre (padrão Spring Boot: 8080).
- Recomendo usar o Maven Wrapper incluído (`./mvnw`).

Verificar versão Java
```bash
java -version
# deve indicar Java 17 (ex.: openjdk version "17.x.x")
```

Build e testes
- Compilar o projeto (inclui testes):
```bash
./mvnw -q test
```
- Compilar sem rodar os testes:
```bash
./mvnw -DskipTests clean package
```
- Rodar apenas os testes (mais verboso):
```bash
./mvnw test
```
- Executar um único teste (JUnit) via Maven (exemplo):
```bash
./mvnw -Dtest=com.example.item_comparation.repository.DataLoaderTest test
```

Rodando a aplicação localmente
- Rodar em modo desenvolvimento (hot reload via spring-boot-devtools):
```bash
./mvnw spring-boot:run
```
- Após subir, a app escutará na porta 8080 por padrão. Logs aparecerão no terminal.

Gerar e executar o JAR
- Gerar o JAR (empacotar):
```bash
./mvnw -DskipTests clean package
```
- Executar o JAR gerado (ajuste o nome do arquivo se mudar a versão):
```bash
java -jar target/item-comparation-0.0.1-SNAPSHOT.jar
```

Endpoints principais
Base: http://localhost:8080/products
- GET /products
  - Descrição: retorna todos os produtos
  - Exemplo:
    ```bash
    curl -sS http://localhost:8080/products
    ```
- GET /products/{productId}
  - Descrição: retorna um produto por id (Long)
  - Exemplo:
    ```bash
    curl -sS http://localhost:8080/products/1
    ```
- GET /products/compare/{productIds}
  - Descrição: recebe uma lista CSV de ids (ex.: `1,2,3`) e retorna a lista de produtos na mesma ordem
  - Exemplo:
    ```bash
    curl -sS "http://localhost:8080/products/compare/1,2,3"
    ```
  - Se a lista ficar vazia a API retorna HTTP 204 No Content.

Observações sobre erros HTTP
- `ProductNotFoundException` -> mapeado para 404 (tratado por `GlobalHandlerController`).
- Erros de parsing do CSV (entrada inválida) devem resultar em 400 (ou IllegalArgumentException) — revise `ProductsService` para garantir mapeamento correto.
- Erros inesperados geram 500 (Internal Server Error).

Dados iniciais e loader
- No startup, `DataLoader` (implementa `CommandLineRunner`) lê `src/main/resources/products.json` e chama `ProductsService.saveAll(...)`.
- Se `products.json` estiver ausente ou inválido, `DataLoader` lança `DataLoadException` e a aplicação pode falhar na inicialização.

Executando em uma porta diferente
```bash
# definir porta via variável de ambiente
export SERVER_PORT=9090
./mvnw spring-boot:run
# ou ao executar o jar
java -jar target/item-comparation-0.0.1-SNAPSHOT.jar --server.port=9090
```

Execução em IDE (IntelliJ IDEA)
- Abra o projeto com `File → Open...` e selecione o `pom.xml` do projeto.
- A IDE vai detectar as configurações Maven e o JDK. Se necessário, configure o JDK como Java 17.
- Use a configuração de execução gerada (`ItemComparationApplication`) para rodar a aplicação.

Debug
- Na IDE, rode em modo Debug; pontos de interrupção funcionarão normalmente.
- Ao rodar via `java -jar`, você pode usar a porta remota de debug (opcional) adicionando opções JVM.

Troubleshooting (erros comuns)
- Falha na compilação / erro de versão Java:
  - Verifique `java -version` e ajuste `JAVA_HOME` para apontar ao JDK 17.
- Porta já em uso:
  - Mude `SERVER_PORT` ou libere a porta 8080.
- Erro ao carregar `products.json` (DataLoadException):
  - Verifique que `src/main/resources/products.json` existe e é JSON válido.
  - Verifique permissões de leitura.
- Testes falhando:
  - Execute os testes em modo verbose: `./mvnw test` e leia o stacktrace.
- Problema com IDE (arquivos `.idea`):
  - Se houver conflitos do `.idea`, remova ou ignore com `.gitignore` (veja `prompts.md` para comandos que usei durante a sessão).

Comandos úteis rápidos
```bash
# abrir logs/console
./mvnw spring-boot:run

# rodar apenas um teste
./mvnw -Dtest=com.example.item_comparation.service.ProductsServiceTest test

# limpar e compilar
./mvnw clean package
```
# Fim do run.md