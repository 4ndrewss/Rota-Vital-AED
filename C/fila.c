#include "fila.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int texto_preenchido(const char *texto, size_t tamanhoMaximo) {
    size_t tamanho = strlen(texto);
    return tamanho > 0 && tamanho < tamanhoMaximo;
}

static int requisicao_valida(const Requisicao *requisicao) {
    return texto_preenchido(requisicao->hospital, TAM_HOSPITAL)
        && texto_preenchido(requisicao->responsavel, TAM_RESPONSAVEL)
        && tipo_sanguineo_valido(requisicao->tipoSanguineo)
        && componente_valido(requisicao->componente)
        && requisicao->quantidade > 0
        && data_valida(requisicao->data);
}

FilaRequisicoes *fila_criar(void) {
    FilaRequisicoes *fila = (FilaRequisicoes *) malloc(sizeof(FilaRequisicoes));
    if (fila == NULL) {
        return NULL;
    }
    fila->inicio = NULL;
    fila->fim = NULL;
    fila->tamanho = 0;
    fila->proximoId = 1;
    return fila;
}

void fila_destruir(FilaRequisicoes **fila) {
    NoRequisicao *atual;
    NoRequisicao *proximo;

    if (fila == NULL || *fila == NULL) {
        return;
    }
    atual = (*fila)->inicio;
    while (atual != NULL) {
        proximo = atual->proximo; /* guarda antes de liberar o nó */
        free(atual);
        atual = proximo;
    }
    free(*fila);
    *fila = NULL;
}

ResultadoFila fila_enfileirar(FilaRequisicoes *fila, const Requisicao *requisicao, int *idGerado) {
    NoRequisicao *novo;

    if (fila == NULL || requisicao == NULL || !requisicao_valida(requisicao)) {
        return FILA_PARAMETRO_INVALIDO;
    }

    novo = (NoRequisicao *) malloc(sizeof(NoRequisicao));
    if (novo == NULL) {
        return FILA_ERRO_MEMORIA;
    }
    novo->requisicao = *requisicao;
    novo->requisicao.id = fila->proximoId++;
    novo->proximo = NULL;

    if (fila->fim == NULL) {
        /* Fila vazia: o novo nó é ao mesmo tempo início e fim. */
        fila->inicio = novo;
    } else {
        fila->fim->proximo = novo;
    }
    fila->fim = novo;
    fila->tamanho++;

    if (idGerado != NULL) {
        *idGerado = novo->requisicao.id;
    }
    return FILA_OK;
}

ResultadoFila fila_desenfileirar(FilaRequisicoes *fila, Requisicao *saida) {
    NoRequisicao *removido;

    if (fila == NULL) {
        return FILA_PARAMETRO_INVALIDO;
    }
    if (fila->inicio == NULL) {
        return FILA_VAZIA;
    }

    removido = fila->inicio;
    if (saida != NULL) {
        *saida = removido->requisicao;
    }

    fila->inicio = removido->proximo;
    if (fila->inicio == NULL) {
        /* Removemos o último nó: `fim` não pode continuar apontando para memória liberada. */
        fila->fim = NULL;
    }
    free(removido);
    fila->tamanho--;
    return FILA_OK;
}

const Requisicao *fila_frente(const FilaRequisicoes *fila) {
    if (fila == NULL || fila->inicio == NULL) {
        return NULL;
    }
    return &fila->inicio->requisicao;
}

const Requisicao *fila_buscar(const FilaRequisicoes *fila, int id) {
    NoRequisicao *atual;

    if (fila == NULL) {
        return NULL;
    }
    for (atual = fila->inicio; atual != NULL; atual = atual->proximo) {
        if (atual->requisicao.id == id) {
            return &atual->requisicao;
        }
    }
    return NULL;
}

int fila_posicao(const FilaRequisicoes *fila, int id) {
    NoRequisicao *atual;
    int posicao = 1;

    if (fila == NULL) {
        return 0;
    }
    for (atual = fila->inicio; atual != NULL; atual = atual->proximo, posicao++) {
        if (atual->requisicao.id == id) {
            return posicao;
        }
    }
    return 0;
}

int fila_contar_por_hospital(const FilaRequisicoes *fila, const char *hospital) {
    NoRequisicao *atual;
    int total = 0;

    if (fila == NULL || hospital == NULL) {
        return 0;
    }
    for (atual = fila->inicio; atual != NULL; atual = atual->proximo) {
        if (strcmp(atual->requisicao.hospital, hospital) == 0) {
            total++;
        }
    }
    return total;
}

int fila_vazia(const FilaRequisicoes *fila) {
    return fila == NULL || fila->inicio == NULL;
}

void fila_imprimir(const FilaRequisicoes *fila) {
    NoRequisicao *atual;
    int posicao = 1;

    if (fila == NULL) {
        return;
    }
    printf("Fila de requisicoes (%d pendente(s)):\n", fila->tamanho);
    if (fila->inicio == NULL) {
        printf("  (vazia)\n");
        return;
    }
    printf("  %-3s %-4s %-24s %-4s %-16s %4s  %s\n",
           "POS", "ID", "HOSPITAL", "TIPO", "COMPONENTE", "QTD", "DATA");
    for (atual = fila->inicio; atual != NULL; atual = atual->proximo, posicao++) {
        printf("  %-3d %-4d %-24s %-4s %-16s %4d  %s\n",
               posicao,
               atual->requisicao.id,
               atual->requisicao.hospital,
               atual->requisicao.tipoSanguineo,
               nome_componente(atual->requisicao.componente),
               atual->requisicao.quantidade,
               atual->requisicao.data);
    }
}

const char *fila_mensagem(ResultadoFila resultado) {
    switch (resultado) {
        case FILA_OK:                 return "Operacao realizada com sucesso";
        case FILA_ERRO_MEMORIA:       return "Falha ao alocar memoria";
        case FILA_PARAMETRO_INVALIDO: return "Parametro invalido";
        case FILA_VAZIA:              return "Fila vazia";
    }
    return "Erro desconhecido";
}
