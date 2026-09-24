#ifndef ESTOQUE_H
#define ESTOQUE_H

/*
 * Estoque de hemocomponentes — lista simplesmente encadeada.
 *
 * Novos itens entram no fim da lista, mantendo a ordem de cadastro.
 */

#include "comum.h"

#define TAM_CODIGO 16

typedef struct {
    char codigo[TAM_CODIGO];                  /* identificador único do lote */
    char tipoSanguineo[TAM_TIPO_SANGUINEO];   /* A+, A-, B+, B-, AB+, AB-, O+, O- */
    TipoHemocomponente componente;
    int quantidade;                           /* número de bolsas */
    char validade[TAM_DATA];                  /* AAAA-MM-DD */
} ItemEstoque;

typedef struct NoEstoque {
    ItemEstoque item;
    struct NoEstoque *proximo;
} NoEstoque;

typedef struct {
    NoEstoque *inicio;
    int tamanho;
} Estoque;

typedef enum {
    ESTOQUE_OK = 0,
    ESTOQUE_ERRO_MEMORIA,
    ESTOQUE_PARAMETRO_INVALIDO,
    ESTOQUE_CODIGO_DUPLICADO,
    ESTOQUE_NAO_ENCONTRADO,
    ESTOQUE_QUANTIDADE_INSUFICIENTE
} ResultadoEstoque;

/* Aloca um estoque vazio. Retorna NULL se não houver memória. */
Estoque *estoque_criar(void);

/* Libera todos os nós e o próprio estoque; deixa *estoque == NULL. */
void estoque_destruir(Estoque **estoque);

/* Insere uma cópia de `item` no fim da lista. */
ResultadoEstoque estoque_inserir(Estoque *estoque, const ItemEstoque *item);

/* Remove (e libera) o item com o código informado. */
ResultadoEstoque estoque_remover(Estoque *estoque, const char *codigo);

/* Dá baixa em `quantidade` bolsas do item; remove o nó se zerar. */
ResultadoEstoque estoque_retirar(Estoque *estoque, const char *codigo, int quantidade);

/* Retorna o nó com o código informado, ou NULL se não existir. */
NoEstoque *estoque_buscar(const Estoque *estoque, const char *codigo);

/* Soma as bolsas disponíveis de um tipo sanguíneo + hemocomponente. */
int estoque_total_disponivel(const Estoque *estoque, const char *tipoSanguineo,
                             TipoHemocomponente componente);

void estoque_imprimir(const Estoque *estoque);

const char *estoque_mensagem(ResultadoEstoque resultado);

#endif
