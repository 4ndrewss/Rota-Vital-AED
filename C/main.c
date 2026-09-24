#include "estoque.h"
#include "fila.h"
#include "pilha.h"

#include <stdio.h>
#include <string.h>

#define HOJE "2026-09-23"

static ItemEstoque novo_item(const char *codigo, const char *tipo, TipoHemocomponente componente,
                             int quantidade, const char *validade) {
    ItemEstoque item;
    memset(&item, 0, sizeof(item));
    strncpy(item.codigo, codigo, TAM_CODIGO - 1);
    strncpy(item.tipoSanguineo, tipo, TAM_TIPO_SANGUINEO - 1);
    item.componente = componente;
    item.quantidade = quantidade;
    strncpy(item.validade, validade, TAM_DATA - 1);
    return item;
}

static Requisicao nova_requisicao(const char *hospital, const char *responsavel, const char *tipo,
                                  TipoHemocomponente componente, int quantidade, const char *data) {
    Requisicao requisicao;
    memset(&requisicao, 0, sizeof(requisicao));
    strncpy(requisicao.hospital, hospital, TAM_HOSPITAL - 1);
    strncpy(requisicao.responsavel, responsavel, TAM_RESPONSAVEL - 1);
    strncpy(requisicao.tipoSanguineo, tipo, TAM_TIPO_SANGUINEO - 1);
    requisicao.componente = componente;
    requisicao.quantidade = quantidade;
    strncpy(requisicao.data, data, TAM_DATA - 1);
    return requisicao;
}

static void registrar(PilhaHistorico *historico, TipoOperacao tipo, const char *referencia,
                      int quantidade, const char *descricao) {
    Operacao operacao;
    memset(&operacao, 0, sizeof(operacao));
    operacao.tipo = tipo;
    strncpy(operacao.referencia, referencia, TAM_REFERENCIA - 1);
    operacao.quantidade = quantidade;
    strcpy(operacao.data, HOJE);
    strncpy(operacao.descricao, descricao, TAM_DESCRICAO - 1);
    pilha_empilhar(historico, &operacao, NULL);
}

static void inserir(Estoque *estoque, PilhaHistorico *historico, ItemEstoque item) {
    char descricao[TAM_DESCRICAO];
    ResultadoEstoque r = estoque_inserir(estoque, &item);

    printf("Inserir %-7s -> %s\n", item.codigo, estoque_mensagem(r));
    if (r == ESTOQUE_OK) {
        snprintf(descricao, sizeof(descricao), "%s %s, validade %s",
                 nome_componente(item.componente), item.tipoSanguineo, item.validade);
        registrar(historico, OP_ENTRADA_ESTOQUE, item.codigo, item.quantidade, descricao);
    }
}

static void remover(Estoque *estoque, PilhaHistorico *historico, const char *codigo,
                    const char *motivo) {
    NoEstoque *no = estoque_buscar(estoque, codigo);
    int quantidade = no != NULL ? no->item.quantidade : 0;
    ResultadoEstoque r = estoque_remover(estoque, codigo);

    printf("Remover %-7s -> %s\n", codigo, estoque_mensagem(r));
    if (r == ESTOQUE_OK) {
        registrar(historico, OP_REMOCAO_ESTOQUE, codigo, quantidade, motivo);
    }
}

static void enfileirar(FilaRequisicoes *fila, PilhaHistorico *historico, Requisicao requisicao) {
    char referencia[TAM_REFERENCIA];
    int id = 0;
    ResultadoFila r = fila_enfileirar(fila, &requisicao, &id);

    if (r == FILA_OK) {
        printf("Requisicao #%d (%s) enfileirada\n", id, requisicao.hospital);
        snprintf(referencia, sizeof(referencia), "REQ-%d", id);
        registrar(historico, OP_REQUISICAO_RECEBIDA, referencia, requisicao.quantidade,
                  requisicao.hospital);
    } else {
        printf("Requisicao de %s rejeitada -> %s\n", requisicao.hospital, fila_mensagem(r));
    }
}

/*
 * Atende a requisição retirando bolsas dos lotes que vencem primeiro (FEFO).
 * Só dá baixa se o estoque cobrir o pedido inteiro.
 */
static void atender(Estoque *estoque, PilhaHistorico *historico, const Requisicao *requisicao) {
    char referencia[TAM_REFERENCIA];
    char descricao[TAM_DESCRICAO];
    int restante = requisicao->quantidade;
    NoEstoque *lote;
    int retirar;

    snprintf(referencia, sizeof(referencia), "REQ-%d", requisicao->id);
    printf("Atendendo #%d %s: %d bolsa(s) de %s %s -> ",
           requisicao->id, requisicao->hospital, requisicao->quantidade,
           nome_componente(requisicao->componente), requisicao->tipoSanguineo);

    if (estoque_total_disponivel(estoque, requisicao->tipoSanguineo, requisicao->componente)
        < requisicao->quantidade) {
        printf("estoque insuficiente\n");
        registrar(historico, OP_REQUISICAO_RECUSADA, referencia, requisicao->quantidade,
                  "Estoque insuficiente");
        return;
    }

    while (restante > 0) {
        lote = estoque_proximo_a_vencer(estoque, requisicao->tipoSanguineo, requisicao->componente);
        retirar = lote->item.quantidade < restante ? lote->item.quantidade : restante;
        printf("%s(%d) ", lote->item.codigo, retirar);
        snprintf(descricao, sizeof(descricao), "Para %s", referencia);
        registrar(historico, OP_RETIRADA_ESTOQUE, lote->item.codigo, retirar, descricao);
        estoque_retirar(estoque, lote->item.codigo, retirar);
        restante -= retirar;
    }
    printf("\n");
    registrar(historico, OP_REQUISICAO_ATENDIDA, referencia, requisicao->quantidade,
              requisicao->hospital);
}

/* Desfaz a última operação se ela for uma entrada no estoque (cadastro feito por engano). */
static void desfazer_ultima(Estoque *estoque, PilhaHistorico *historico) {
    const Operacao *topo = pilha_topo(historico);
    Operacao desfeita;

    if (topo == NULL) {
        printf("Nada para desfazer.\n");
        return;
    }
    if (topo->tipo != OP_ENTRADA_ESTOQUE) {
        printf("Ultima operacao (%s) nao pode ser desfeita.\n", nome_operacao(topo->tipo));
        return;
    }
    pilha_desempilhar(historico, &desfeita);
    printf("Desfazendo #%d %s %s -> %s\n", desfeita.sequencia, nome_operacao(desfeita.tipo),
           desfeita.referencia, estoque_mensagem(estoque_remover(estoque, desfeita.referencia)));
}

int main(void) {
    Estoque *estoque = estoque_criar();
    FilaRequisicoes *fila = fila_criar();
    PilhaHistorico *historico = pilha_criar();
    const Requisicao *frente;
    Requisicao atendida;
    NoEstoque *no;

    if (estoque == NULL || fila == NULL || historico == NULL) {
        fprintf(stderr, "Falha ao alocar as estruturas.\n");
        estoque_destruir(&estoque);
        fila_destruir(&fila);
        pilha_destruir(&historico);
        return 1;
    }

    printf("=== Rota Vital: Estoque de Hemocomponentes ===\n\n");

    inserir(estoque, historico, novo_item("HM-0001", "O-", HEMACIAS, 8, "2026-11-05"));
    inserir(estoque, historico, novo_item("HM-0002", "A+", HEMACIAS, 12, "2026-10-18"));
    inserir(estoque, historico, novo_item("PL-0001", "AB+", PLASMA, 20, "2027-08-01"));
    inserir(estoque, historico, novo_item("PQ-0001", "O-", PLAQUETAS, 3, "2026-09-28"));
    inserir(estoque, historico, novo_item("HM-0003", "O-", HEMACIAS, 5, "2026-10-02"));
    inserir(estoque, historico, novo_item("HM-0003", "B-", HEMACIAS, 1, "2026-10-10")); /* duplicado */
    inserir(estoque, historico, novo_item("XX-0001", "Z+", HEMACIAS, 1, "2026-10-10")); /* tipo inválido */

    printf("\n");
    estoque_imprimir(estoque);

    printf("\nConsulta: hemacias O- disponiveis = %d bolsa(s)\n",
           estoque_total_disponivel(estoque, "O-", HEMACIAS));

    no = estoque_proximo_a_vencer(estoque, "O-", HEMACIAS);
    if (no != NULL) {
        printf("Proxima a vencer (O-, hemacias): %s em %s\n\n", no->item.codigo, no->item.validade);
    }

    remover(estoque, historico, "PQ-0001", "Descarte: bolsa danificada");
    remover(estoque, historico, "ZZ-9999", "Inexistente");

    printf("\n=== Rota Vital: Fila de Requisicoes Hospitalares ===\n\n");

    enfileirar(fila, historico, nova_requisicao("Hospital da Restauracao", "Dr. Silva", "O-", HEMACIAS, 7, HOJE));
    enfileirar(fila, historico, nova_requisicao("IMIP", "Dra. Souza", "A+", HEMACIAS, 4, HOJE));
    enfileirar(fila, historico, nova_requisicao("Hospital Getulio Vargas", "Dr. Lima", "B-", PLAQUETAS, 2, HOJE));
    enfileirar(fila, historico, nova_requisicao("IMIP", "Dra. Souza", "AB+", PLASMA, 5, HOJE));
    enfileirar(fila, historico, nova_requisicao("Hospital Otavio de Freitas", "", "O+", HEMACIAS, 1, HOJE)); /* sem responsável */

    printf("\n");
    fila_imprimir(fila);

    frente = fila_frente(fila);
    if (frente != NULL) {
        printf("\nProxima a ser atendida: #%d (%s)\n", frente->id, frente->hospital);
    }
    printf("Posicao da requisicao #4: %d\n", fila_posicao(fila, 4));
    printf("Requisicoes pendentes do IMIP: %d\n\n", fila_contar_por_hospital(fila, "IMIP"));

    while (fila_desenfileirar(fila, &atendida) == FILA_OK) {
        atender(estoque, historico, &atendida);
    }

    printf("\n");
    fila_imprimir(fila);
    printf("\n");
    estoque_imprimir(estoque);

    printf("\n=== Rota Vital: Historico de Operacoes ===\n\n");

    inserir(estoque, historico, novo_item("HM-0099", "O+", HEMACIAS, 2, "2026-12-15")); /* por engano */
    desfazer_ultima(estoque, historico);
    desfazer_ultima(estoque, historico); /* topo agora é um atendimento: não desfaz */

    printf("\nRequisicoes atendidas: %d | recusadas: %d\n\n",
           pilha_contar_por_tipo(historico, OP_REQUISICAO_ATENDIDA),
           pilha_contar_por_tipo(historico, OP_REQUISICAO_RECUSADA));
    pilha_imprimir(historico);

    pilha_destruir(&historico);
    fila_destruir(&fila);
    estoque_destruir(&estoque);
    return 0;
}
