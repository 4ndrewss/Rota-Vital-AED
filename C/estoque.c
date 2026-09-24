#include "estoque.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int item_valido(const ItemEstoque *item) {
    size_t tamCodigo = strlen(item->codigo);
    return tamCodigo > 0 && tamCodigo < TAM_CODIGO
        && tipo_sanguineo_valido(item->tipoSanguineo)
        && componente_valido(item->componente)
        && item->quantidade > 0
        && data_valida(item->validade);
}

Estoque *estoque_criar(void) {
    Estoque *estoque = (Estoque *) malloc(sizeof(Estoque));
    if (estoque == NULL) {
        return NULL;
    }
    estoque->inicio = NULL;
    estoque->tamanho = 0;
    return estoque;
}

void estoque_destruir(Estoque **estoque) {
    NoEstoque *atual;
    NoEstoque *proximo;

    if (estoque == NULL || *estoque == NULL) {
        return;
    }
    atual = (*estoque)->inicio;
    while (atual != NULL) {
        proximo = atual->proximo; /* guarda antes de liberar o nó */
        free(atual);
        atual = proximo;
    }
    free(*estoque);
    *estoque = NULL;
}

ResultadoEstoque estoque_inserir(Estoque *estoque, const ItemEstoque *item) {
    NoEstoque *novo;
    NoEstoque *ultimo;

    if (estoque == NULL || item == NULL || !item_valido(item)) {
        return ESTOQUE_PARAMETRO_INVALIDO;
    }
    if (estoque_buscar(estoque, item->codigo) != NULL) {
        return ESTOQUE_CODIGO_DUPLICADO;
    }

    novo = (NoEstoque *) malloc(sizeof(NoEstoque));
    if (novo == NULL) {
        return ESTOQUE_ERRO_MEMORIA;
    }
    novo->item = *item;
    novo->proximo = NULL;

    if (estoque->inicio == NULL) {
        estoque->inicio = novo; /* lista vazia: novo nó vira a cabeça */
    } else {
        ultimo = estoque->inicio;
        while (ultimo->proximo != NULL) {
            ultimo = ultimo->proximo;
        }
        ultimo->proximo = novo;
    }
    estoque->tamanho++;
    return ESTOQUE_OK;
}

ResultadoEstoque estoque_remover(Estoque *estoque, const char *codigo) {
    NoEstoque *anterior = NULL;
    NoEstoque *atual;

    if (estoque == NULL || codigo == NULL) {
        return ESTOQUE_PARAMETRO_INVALIDO;
    }

    atual = estoque->inicio;
    while (atual != NULL && strcmp(atual->item.codigo, codigo) != 0) {
        anterior = atual;
        atual = atual->proximo;
    }
    if (atual == NULL) {
        return ESTOQUE_NAO_ENCONTRADO;
    }

    if (anterior == NULL) {
        estoque->inicio = atual->proximo;
    } else {
        anterior->proximo = atual->proximo;
    }
    free(atual);
    estoque->tamanho--;
    return ESTOQUE_OK;
}

ResultadoEstoque estoque_retirar(Estoque *estoque, const char *codigo, int quantidade) {
    NoEstoque *no;

    if (estoque == NULL || codigo == NULL || quantidade <= 0) {
        return ESTOQUE_PARAMETRO_INVALIDO;
    }
    no = estoque_buscar(estoque, codigo);
    if (no == NULL) {
        return ESTOQUE_NAO_ENCONTRADO;
    }
    if (no->item.quantidade < quantidade) {
        return ESTOQUE_QUANTIDADE_INSUFICIENTE;
    }

    no->item.quantidade -= quantidade;
    if (no->item.quantidade == 0) {
        return estoque_remover(estoque, codigo);
    }
    return ESTOQUE_OK;
}

NoEstoque *estoque_buscar(const Estoque *estoque, const char *codigo) {
    NoEstoque *atual;

    if (estoque == NULL || codigo == NULL) {
        return NULL;
    }
    for (atual = estoque->inicio; atual != NULL; atual = atual->proximo) {
        if (strcmp(atual->item.codigo, codigo) == 0) {
            return atual;
        }
    }
    return NULL;
}

int estoque_total_disponivel(const Estoque *estoque, const char *tipoSanguineo,
                             TipoHemocomponente componente) {
    NoEstoque *atual;
    int total = 0;

    if (estoque == NULL || tipoSanguineo == NULL) {
        return 0;
    }
    for (atual = estoque->inicio; atual != NULL; atual = atual->proximo) {
        if (atual->item.componente == componente
            && strcmp(atual->item.tipoSanguineo, tipoSanguineo) == 0) {
            total += atual->item.quantidade;
        }
    }
    return total;
}

void estoque_imprimir(const Estoque *estoque) {
    NoEstoque *atual;

    if (estoque == NULL) {
        return;
    }
    printf("Estoque (%d item(ns)):\n", estoque->tamanho);
    if (estoque->inicio == NULL) {
        printf("  (vazio)\n");
        return;
    }
    printf("  %-15s %-4s %-16s %5s  %s\n", "CODIGO", "TIPO", "COMPONENTE", "QTD", "VALIDADE");
    for (atual = estoque->inicio; atual != NULL; atual = atual->proximo) {
        printf("  %-15s %-4s %-16s %5d  %s\n",
               atual->item.codigo,
               atual->item.tipoSanguineo,
               nome_componente(atual->item.componente),
               atual->item.quantidade,
               atual->item.validade);
    }
}

const char *estoque_mensagem(ResultadoEstoque resultado) {
    switch (resultado) {
        case ESTOQUE_OK:                      return "Operacao realizada com sucesso";
        case ESTOQUE_ERRO_MEMORIA:            return "Falha ao alocar memoria";
        case ESTOQUE_PARAMETRO_INVALIDO:      return "Parametro invalido";
        case ESTOQUE_CODIGO_DUPLICADO:        return "Codigo ja cadastrado no estoque";
        case ESTOQUE_NAO_ENCONTRADO:          return "Item nao encontrado";
        case ESTOQUE_QUANTIDADE_INSUFICIENTE: return "Quantidade insuficiente em estoque";
    }
    return "Erro desconhecido";
}
