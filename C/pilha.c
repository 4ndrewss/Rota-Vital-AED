#include "pilha.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int operacao_valida(const Operacao *operacao) {
    size_t tamReferencia = strlen(operacao->referencia);
    return operacao->tipo >= OP_ENTRADA_ESTOQUE && operacao->tipo <= OP_REQUISICAO_RECUSADA
        && tamReferencia > 0 && tamReferencia < TAM_REFERENCIA
        && strlen(operacao->descricao) < TAM_DESCRICAO
        && operacao->quantidade >= 0
        && data_valida(operacao->data);
}

PilhaHistorico *pilha_criar(void) {
    PilhaHistorico *pilha = (PilhaHistorico *) malloc(sizeof(PilhaHistorico));
    if (pilha == NULL) {
        return NULL;
    }
    pilha->topo = NULL;
    pilha->tamanho = 0;
    pilha->proximaSequencia = 1;
    return pilha;
}

void pilha_destruir(PilhaHistorico **pilha) {
    NoOperacao *atual;
    NoOperacao *proximo;

    if (pilha == NULL || *pilha == NULL) {
        return;
    }
    atual = (*pilha)->topo;
    while (atual != NULL) {
        proximo = atual->proximo; /* guarda antes de liberar o nó */
        free(atual);
        atual = proximo;
    }
    free(*pilha);
    *pilha = NULL;
}

ResultadoPilha pilha_empilhar(PilhaHistorico *pilha, const Operacao *operacao, int *sequenciaGerada) {
    NoOperacao *novo;

    if (pilha == NULL || operacao == NULL || !operacao_valida(operacao)) {
        return PILHA_PARAMETRO_INVALIDO;
    }

    novo = (NoOperacao *) malloc(sizeof(NoOperacao));
    if (novo == NULL) {
        return PILHA_ERRO_MEMORIA;
    }
    novo->operacao = *operacao;
    novo->operacao.sequencia = pilha->proximaSequencia++;

    /* O novo nó aponta para o antigo topo e passa a ser o topo. */
    novo->proximo = pilha->topo;
    pilha->topo = novo;
    pilha->tamanho++;

    if (sequenciaGerada != NULL) {
        *sequenciaGerada = novo->operacao.sequencia;
    }
    return PILHA_OK;
}

ResultadoPilha pilha_desempilhar(PilhaHistorico *pilha, Operacao *saida) {
    NoOperacao *removido;

    if (pilha == NULL) {
        return PILHA_PARAMETRO_INVALIDO;
    }
    if (pilha->topo == NULL) {
        return PILHA_VAZIA;
    }

    removido = pilha->topo;
    if (saida != NULL) {
        *saida = removido->operacao;
    }
    pilha->topo = removido->proximo;
    free(removido);
    pilha->tamanho--;
    return PILHA_OK;
}

const Operacao *pilha_topo(const PilhaHistorico *pilha) {
    if (pilha == NULL || pilha->topo == NULL) {
        return NULL;
    }
    return &pilha->topo->operacao;
}

int pilha_contar_por_tipo(const PilhaHistorico *pilha, TipoOperacao tipo) {
    NoOperacao *atual;
    int total = 0;

    if (pilha == NULL) {
        return 0;
    }
    for (atual = pilha->topo; atual != NULL; atual = atual->proximo) {
        if (atual->operacao.tipo == tipo) {
            total++;
        }
    }
    return total;
}

int pilha_vazia(const PilhaHistorico *pilha) {
    return pilha == NULL || pilha->topo == NULL;
}

void pilha_imprimir(const PilhaHistorico *pilha) {
    NoOperacao *atual;

    if (pilha == NULL) {
        return;
    }
    printf("Historico de operacoes (%d registro(s), mais recente primeiro):\n", pilha->tamanho);
    if (pilha->topo == NULL) {
        printf("  (vazio)\n");
        return;
    }
    printf("  %-4s %-20s %-10s %4s  %-10s  %s\n",
           "SEQ", "OPERACAO", "REF", "QTD", "DATA", "DESCRICAO");
    for (atual = pilha->topo; atual != NULL; atual = atual->proximo) {
        printf("  %-4d %-20s %-10s %4d  %-10s  %s\n",
               atual->operacao.sequencia,
               nome_operacao(atual->operacao.tipo),
               atual->operacao.referencia,
               atual->operacao.quantidade,
               atual->operacao.data,
               atual->operacao.descricao);
    }
}

const char *nome_operacao(TipoOperacao tipo) {
    switch (tipo) {
        case OP_ENTRADA_ESTOQUE:     return "Entrada estoque";
        case OP_REMOCAO_ESTOQUE:     return "Remocao estoque";
        case OP_RETIRADA_ESTOQUE:    return "Retirada estoque";
        case OP_REQUISICAO_RECEBIDA: return "Requisicao recebida";
        case OP_REQUISICAO_ATENDIDA: return "Requisicao atendida";
        case OP_REQUISICAO_RECUSADA: return "Requisicao recusada";
    }
    return "Desconhecida";
}

const char *pilha_mensagem(ResultadoPilha resultado) {
    switch (resultado) {
        case PILHA_OK:                 return "Operacao realizada com sucesso";
        case PILHA_ERRO_MEMORIA:       return "Falha ao alocar memoria";
        case PILHA_PARAMETRO_INVALIDO: return "Parametro invalido";
        case PILHA_VAZIA:              return "Historico vazio";
    }
    return "Erro desconhecido";
}
