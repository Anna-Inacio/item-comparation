# Prompts e pedidos (organizado)

Este arquivo reúne, de forma organizada, os prompts e pedidos que foram feitos durante a criação/manutenção deste projeto. Use-o como referência rápida para repetir tarefas, entender o contexto de perguntas que você fez à assistente e decidir próximos passos.

Como usar este arquivo
- Leia as seções abaixo para localizar prompts por categoria (Copilot / Gemini).
- Cada item é um prompt curto que você pode enviar a uma IA (ou usar como checklist).
- Ao lado de cada prompt há uma linha com uma breve descrição do objetivo.

Sumário
- Copilot Chat: prompts relacionados a mudanças no código, testes e geração de artefatos.
- Gemini: perguntas de arquitetura, formatos de dados e melhores práticas.
---

## Copilot Chat (tópicos de desenvolvimento e testes)

1. "Preciso cobrir de teste o trecho selecionado"
   - Objetivo: criar testes unitários que alcancem e verifiquem o bloco de tratamento de exceções (catch) selecionado.

2. "Consigo melhorar algo na classe referenciada?"
   - Objetivo: avaliar a classe em questão e sugerir refatorações (mensagens de erro, extração de métodos, injeção para testes).

3. "Crie para mim um método genérico que crie uma lista de produtos de teste."
   - Objetivo: gerar um helper/factory para criar instâncias de `Product` em testes.

4. "Me explique como funciona o trecho selecionado."
   - Objetivo: obter uma explicação passo-a-passo do comportamento do código (fluxo, exceções, efeitos colaterais).

5. "Crie o curl do endpoint referenciado."
   - Objetivo: produzir exemplos práticos de `curl` para testar endpoints REST da aplicação.

6. "Crie um `run.md` explicando como executar o projeto"
   - Objetivo: documentar build, execução, testes e endpoints para desenvolvedores.

7. "Crie o `README.md` explicando a arquitetura do projeto, principais endpoints e exemplos de uso."
   - Objetivo: gerar um README de alto nível com instruções e descrição da arquitetura.

---

## Gemini (design, arquitetura e dados)

1. "Como usar um json ou um csv como banco de dados usando Java/spring boot?"
   - Objetivo: entender abordagens para usar arquivos (JSON/CSV) como fonte de dados simples em apps Spring.

2. "Como criar um arquivo json para servir como banco de dados?"
   - Objetivo: instruções e formato recomendado para um arquivo `products.json` que sirva como seed/dataset.

3. "Me de exemplos de response de erros customizados e como ficaria a implementação desse erros customizados utilizando java/spring."
   - Objetivo: exemplos de payloads de erro (JSON) e implementação de handlers/exception classes no Spring.

4. "É mais recomendado fazer um getById com um Long id no argumento ou com um long Id e pq?"
   - Objetivo: discutir diferenças entre `Long` e `long` (nullability, binding em controllers) e recomendar a melhor prática.

---