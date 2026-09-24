#ifndef PILHA_H
#define PILHA_H

/*
 * Histórico de operações — pilha implementada com lista encadeada.
 *
 * Política LIFO: a operação mais recente fica no `topo`. Push e pop
 * acontecem no topo, ambos em O(1), o que permite consultar ou desfazer
 * a última operação realizada.
 */

#include "comum.h"

#define TAM_REFERENCIA 16
#define TAM_DESCRICAO 96

typedef enum {
    OP_ENTRADA_ESTOQUE,      /* lote cadastrado no estoque */
    OP_REMOCAO_ESTOQUE,      /* lote removido do estoque (descarte, vencimento) */
    OP_RETIRADA_ESTOQUE,     /* baixa de bolsas de um lote */
    OP_REQUISICAO_RECEBIDA,  /* requisição entrou na fila */
    OP_REQUISICAO_ATENDIDA,
    OP_REQUISICAO_RECUSADA
} TipoOperacao;

typedef struct {
    int sequencia;                        /* atribuída pela pilha ao empilhar */
    TipoOperacao tipo;
    char referencia[TAM_REFERENCIA];      /* código do lote ou id da requisição */
    int quantidade;                       /* bolsas envolvidas (0 se não se aplica) */
    char data[TAM_DATA];                  /* AAAA-MM-DD */
    char descricao[TAM_DESCRICAO];        /* texto livre, opcional */
} Operacao;

typedef struct NoOperacao {
    Operacao operacao;
    struct NoOperacao *proximo;           /* aponta para a operação anterior */
} NoOperacao;

typedef struct {
    NoOperacao *topo;                     /* operação mais recente */
    int tamanho;
    int proximaSequencia;
} PilhaHistorico;

typedef enum {
    PILHA_OK = 0,
    PILHA_ERRO_MEMORIA,
    PILHA_PARAMETRO_INVALIDO,
    PILHA_VAZIA
} ResultadoPilha;

/* Aloca uma pilha vazia. Retorna NULL se não houver memória. */
PilhaHistorico *pilha_criar(void);

/* Libera todos os nós e a própria pilha; deixa *pilha == NULL. */
void pilha_destruir(PilhaHistorico **pilha);

/* Push: empilha uma cópia da operação. Se `sequenciaGerada` != NULL, recebe o número atribuído. */
ResultadoPilha pilha_empilhar(PilhaHistorico *pilha, const Operacao *operacao, int *sequenciaGerada);

/* Pop: remove a operação do topo, copiando-a para `saida` (pode ser NULL). */
ResultadoPilha pilha_desempilhar(PilhaHistorico *pilha, Operacao *saida);

/* Retorna a operação do topo sem removê-la, ou NULL se a pilha estiver vazia. */
const Operacao *pilha_topo(const PilhaHistorico *pilha);

/* Quantas operações do tipo informado existem no histórico. */
int pilha_contar_por_tipo(const PilhaHistorico *pilha, TipoOperacao tipo);

int pilha_vazia(const PilhaHistorico *pilha);

/* Imprime do topo para a base (mais recente primeiro). */
void pilha_imprimir(const PilhaHistorico *pilha);

const char *nome_operacao(TipoOperacao tipo);
const char *pilha_mensagem(ResultadoPilha resultado);

#endif
