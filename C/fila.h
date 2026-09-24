#ifndef FILA_H
#define FILA_H

/*
 * Fila de requisições hospitalares — lista encadeada com ponteiros de início e fim.
 *
 * Política FIFO: as requisições são atendidas na ordem de chegada.
 * Enfileirar ocorre no `fim` e desenfileirar no `inicio`, ambos em O(1).
 */

#include "comum.h"

#define TAM_HOSPITAL 64
#define TAM_RESPONSAVEL 64

typedef struct {
    int id;                                   /* atribuído pela fila ao enfileirar */
    char hospital[TAM_HOSPITAL];
    char responsavel[TAM_RESPONSAVEL];
    char tipoSanguineo[TAM_TIPO_SANGUINEO];   /* A+, A-, B+, B-, AB+, AB-, O+, O- */
    TipoHemocomponente componente;
    int quantidade;                           /* número de bolsas solicitadas */
    char data[TAM_DATA];                      /* AAAA-MM-DD */
} Requisicao;

typedef struct NoRequisicao {
    Requisicao requisicao;
    struct NoRequisicao *proximo;
} NoRequisicao;

typedef struct {
    NoRequisicao *inicio;   /* próxima a ser atendida */
    NoRequisicao *fim;      /* última que chegou */
    int tamanho;
    int proximoId;
} FilaRequisicoes;

typedef enum {
    FILA_OK = 0,
    FILA_ERRO_MEMORIA,
    FILA_PARAMETRO_INVALIDO,
    FILA_VAZIA
} ResultadoFila;

/* Aloca uma fila vazia. Retorna NULL se não houver memória. */
FilaRequisicoes *fila_criar(void);

/* Libera todos os nós e a própria fila; deixa *fila == NULL. */
void fila_destruir(FilaRequisicoes **fila);

/* Insere uma cópia da requisição no fim. Se `idGerado` != NULL, recebe o id atribuído. */
ResultadoFila fila_enfileirar(FilaRequisicoes *fila, const Requisicao *requisicao, int *idGerado);

/* Remove a requisição do início, copiando-a para `saida` (pode ser NULL). */
ResultadoFila fila_desenfileirar(FilaRequisicoes *fila, Requisicao *saida);

/* Retorna a requisição do início sem removê-la, ou NULL se a fila estiver vazia. */
const Requisicao *fila_frente(const FilaRequisicoes *fila);

/* Retorna a requisição com o id informado, ou NULL. */
const Requisicao *fila_buscar(const FilaRequisicoes *fila, int id);

/* Posição (1 = próxima a ser atendida) da requisição com o id, ou 0 se não existir. */
int fila_posicao(const FilaRequisicoes *fila, int id);

/* Quantas requisições pendentes existem para o hospital informado. */
int fila_contar_por_hospital(const FilaRequisicoes *fila, const char *hospital);

int fila_vazia(const FilaRequisicoes *fila);

void fila_imprimir(const FilaRequisicoes *fila);

const char *fila_mensagem(ResultadoFila resultado);

#endif
