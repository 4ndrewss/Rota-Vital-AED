# Rota Vital — AED (Unidade 1)

Projeto Integrador Rota Vital (CESAR School — ADS, 3º período): sistema de gestão e distribuição de hemocomponentes.

Esta entrega é referente à disciplina de Algoritmos e Estruturas de Dados (AED), Unidade 1: implementação das estruturas básicas do domínio (estoque, requisições e histórico) em C, com reimplementação equivalente em Java.

## Organização

- [`C/`](./C/) — implementação das estruturas em C (malloc/free, ponteiros explícitos).
- [`Java/`](./Java/) — reimplementação das mesmas estruturas em Java.

## Estruturas

- **Estoque** → lista encadeada
- **Requisições hospitalares** → fila
- **Histórico de operações** → pilha

## Implementação em C (Estoque, Fila e Pilha)

Código em [`C/`](./C/):
- `comum.h` / `comum.c`: tipos compartilhados (hemocomponente) e validações (tipo sanguíneo, data `AAAA-MM-DD`).
- `estoque.h` / `estoque.c` — **lista encadeada**: nó `NoEstoque`, `malloc`/`free`, inserir, remover, retirar bolsas e consultar (busca por código, total por tipo sanguíneo + hemocomponente). Novos itens entram no fim da lista.
- `fila.h` / `fila.c` — **fila** de requisições hospitalares: nó `NoRequisicao`, ponteiros `inicio` e `fim`, enqueue/dequeue em O(1), consultas (frente, busca por id, posição na fila, pendências por hospital). Ordem FIFO.
- `pilha.h` / `pilha.c` — **pilha** do histórico de operações: nó `NoOperacao`, ponteiro `topo`, push/pop em O(1), consultas (topo, contagem por tipo). Ordem LIFO — permite desfazer a última operação.
- `main.c`: demonstração — mostra inserir/remover/consultar em cada estrutura separadamente (estoque, fila e histórico, com exemplo de desfazer).
- `teste.h` + `teste_estoque.c`, `teste_fila.c`, `teste_pilha.c`: casos de teste de inserir/remover/consultar para cada estrutura, com saída `[PASSOU]`/`[FALHOU]` por caso.

### Rodando os testes pelo terminal

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

### Rodando a demonstração

```
cd C
mkdir bin         # só na primeira vez (os scripts de teste também criam)
gcc -std=c99 -o bin/rotavital main.c estoque.c fila.c pilha.c comum.c
bin\rotavital     # Windows
./bin/rotavital   # Linux, macOS, WSL ou Git Bash
```
