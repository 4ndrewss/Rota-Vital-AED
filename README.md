# 🩸 Rota Vital — AED (Unidade 1)

Projeto Integrador Rota Vital (CESAR School — ADS, 3º período): sistema de gestão e distribuição de hemocomponentes.

Esta entrega é referente à disciplina de Algoritmos e Estruturas de Dados (AED), Unidade 1: implementação das estruturas básicas do domínio (estoque, requisições e histórico) em C, com reimplementação equivalente em Java.

## 🗂️ Organização

- [`C/`](./C/) — implementação das estruturas em C (malloc/free, ponteiros explícitos).
- [`Java/`](./Java/) — reimplementação das mesmas estruturas em Java.

## 🧱 Estruturas

- **Estoque** → lista encadeada
- **Requisições hospitalares** → fila
- **Histórico de operações** → pilha

## ⚙️ Implementação em C (Estoque, Fila e Pilha)

Código em [`C/`](./C/):
- `comum.h` / `comum.c`: tipos compartilhados (hemocomponente) e validações (tipo sanguíneo, data `AAAA-MM-DD`).
- `estoque.h` / `estoque.c` — **lista encadeada**: nó `NoEstoque`, `malloc`/`free`, inserir, remover, retirar bolsas e consultar (busca por código, total por tipo sanguíneo + hemocomponente). Novos itens entram no fim da lista.
- `fila.h` / `fila.c` — **fila** de requisições hospitalares: nó `NoRequisicao`, ponteiros `inicio` e `fim`, enqueue/dequeue em O(1), consultas (frente, busca por id, posição na fila, pendências por hospital). Ordem FIFO.
- `pilha.h` / `pilha.c` — **pilha** do histórico de operações: nó `NoOperacao`, ponteiro `topo`, push/pop em O(1), consultas (topo, contagem por tipo). Ordem LIFO — permite desfazer a última operação.
- `main.c`: demonstração — mostra inserir/remover/consultar em cada estrutura separadamente (estoque, fila e histórico, com exemplo de desfazer).
- `teste.h` + `teste_estoque.c`, `teste_fila.c`, `teste_pilha.c`: casos de teste de inserir/remover/consultar para cada estrutura, com saída `[PASSOU]`/`[FALHOU]` por caso.

### ▶️ Rodando os testes pelo terminal

Requer `gcc` no PATH (MinGW/MSYS2 no Windows).

```
cd C
testar.bat        # Windows (cmd ou PowerShell)
./testar.sh       # Linux, macOS, WSL ou Git Bash
```

Saída esperada (trecho):

```
=== Testes: Estoque (lista encadeada) ===

 Inserir
  [PASSOU] Inserir em lista vazia
  [PASSOU] Inserir no fim mantem ordem de cadastro
  ...
Resultado: 14/14 casos passaram
...
=== TODOS OS TESTES PASSARAM ===
```

O script retorna código de saída 0 se tudo passar e 1 se algum caso falhar.

### 🖥️ Rodando a demonstração

```
cd C
mkdir bin         # só na primeira vez (os scripts de teste também criam)
gcc -std=c99 -o bin/rotavital main.c estoque.c fila.c pilha.c comum.c
bin\rotavital     # Windows
./bin/rotavital   # Linux, macOS, WSL ou Git Bash
```

## ☕ Implementação em Java (Estoque, Fila e Pilha)

Código em [`Java/src/br/org/cesar/rotavital/`](./Java/src/br/org/cesar/rotavital/):
- `comum/TipoHemocomponente.java` e `comum/Validacao.java`: equivalentes a `comum.h` / `comum.c`.
- `estoque/ItemEstoque.java`: modelo do lote (equivalente ao struct `ItemEstoque` de `estoque.h`).
- `estoque/Estoque.java` — **lista encadeada** com nós próprios (sem `java.util.LinkedList`), referência `inicio`, inserir no fim, remover, retirar bolsas e as mesmas consultas da versão em C (busca por código, total por tipo sanguíneo + hemocomponente). Dados inválidos lançam `IllegalArgumentException`; código duplicado, item inexistente e quantidade insuficiente lançam `EstoqueException` (com o `Motivo`, equivalente a `ResultadoEstoque` em C).
- `estoque/TesteEstoque.java`: casos de teste equivalentes a `teste_estoque.c`, com a mesma saída `[PASSOU]`/`[FALHOU]`.
- `fila/Requisicao.java`: modelo da requisição (POJO com construtor vazio e getters/setters, pronto para virar JSON no Spring Boot).
- `fila/FilaRequisicoes.java` — **fila** com nós próprios (sem `java.util.Queue`), referências `inicio` e `fim`, `enfileirar`/`desenfileirar` em O(1) e as mesmas consultas da versão em C. Dados inválidos lançam `IllegalArgumentException` e desenfileirar fila vazia lança `FilaVaziaException`. Os métodos não usam `synchronized` nesta entrega (Unidade 1) — controle de concorrência fica fora do escopo de AED e pode ser adicionado depois, se a classe virar um `@Service` do Spring.
- `fila/TesteFila.java`: casos de teste equivalentes a `teste_fila.c`, com a mesma saída `[PASSOU]`/`[FALHOU]`.
- `historico/TipoOperacao.java` e `historico/Operacao.java`: equivalentes ao enum `TipoOperacao` e ao struct `Operacao` de `pilha.h`.
- `historico/PilhaHistorico.java` — **pilha** com nós próprios (sem `java.util.Stack`/`Deque`), referência `topo`, `empilhar`/`desempilhar` em O(1) e as mesmas consultas da versão em C (topo, contagem por tipo). Dados inválidos lançam `IllegalArgumentException` e desempilhar histórico vazio lança `PilhaVaziaException`.
- `historico/TestePilha.java`: casos de teste equivalentes a `teste_pilha.c`, com a mesma saída `[PASSOU]`/`[FALHOU]`.

### ▶️ Rodando os testes em Java

Requer JDK 11+ no PATH. No Git Bash, Linux ou macOS:

```
cd Java
javac -d bin $(find src -name "*.java")
java -cp bin br.org.cesar.rotavital.estoque.TesteEstoque
java -cp bin br.org.cesar.rotavital.fila.TesteFila
java -cp bin br.org.cesar.rotavital.historico.TestePilha
```

No PowerShell, troque a linha do `javac` por:

```
javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
```

---

## 👥 Membros da Equipe

| Integrante | GitHub |
|---|---|
| Andrews Queiroz | [@4ndrewss](https://github.com/4ndrewss) |
| Caio Gilles | [@CaioGilles](https://github.com/CaioGilles) |
| Enzo Amorim | [@ENZOBRS](https://github.com/ENZOBRS) |
| Gabriela Bayo | [@gabibayo](https://github.com/gabibayo) |
| Glauco Santos| [@glaucosantos002](https://github.com/glaucosantos002) |
| Gustavo Veloso | [@velosogustavo](https://github.com/velosogustavo) |
| Hilton Resende | [@HResende23](https://github.com/HResende23) |

---

## 📌 Gestão e Organização

O acompanhamento das etapas de construção do Rota Vital, a divisão técnica da equipe e o backlog
do projeto foram gerenciados via Trello.

📋 **Acesso ao Quadro:** [Acessar Trello da Equipe](https://trello.com/b/2mXGnXOQ/rota-vitalaedav1)

<img width="1277" height="718" alt="image" src="https://github.com/user-attachments/assets/9515d18a-1147-4551-adb4-b8f2a16c0ba7" />

---

## 📄 Relatório Técnico

Toda a tradução comentada C ↔ Java das três estruturas (estoque, fila, histórico):
[`Rota_Vital__Traducoes_C_Java.pdf`](./docs/Rota_Vital__Traducoes_C_Java.pdf)
