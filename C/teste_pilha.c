#include "pilha.h"
#include "teste.h"

#include <string.h>

static Operacao nova_operacao(TipoOperacao tipo, const char *referencia, int quantidade) {
    Operacao operacao;
    memset(&operacao, 0, sizeof(operacao));
    operacao.tipo = tipo;
    strncpy(operacao.referencia, referencia, TAM_REFERENCIA - 1);
    operacao.quantidade = quantidade;
    strcpy(operacao.data, "2026-09-23");
    return operacao;
}

static PilhaHistorico *pilha_com_tres(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao a = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 8);
    Operacao b = nova_operacao(OP_ENTRADA_ESTOQUE, "PL-0001", 20);
    Operacao c = nova_operacao(OP_REQUISICAO_ATENDIDA, "REQ-1", 2);
    pilha_empilhar(pilha, &a, NULL);
    pilha_empilhar(pilha, &b, NULL);
    pilha_empilhar(pilha, &c, NULL);
    return pilha;
}

/* ---------- Criar / destruir ---------- */

static void criar_pilha_vazia(void) {
    PilhaHistorico *pilha = pilha_criar();
    VERIFICAR(pilha != NULL);
    VERIFICAR(pilha->topo == NULL);
    VERIFICAR(pilha->tamanho == 0);
    VERIFICAR(pilha_vazia(pilha));
    pilha_destruir(&pilha);
}

static void destruir_zera_ponteiro(void) {
    PilhaHistorico *pilha = pilha_com_tres();
    pilha_destruir(&pilha);
    VERIFICAR(pilha == NULL);
    pilha_destruir(&pilha); /* destruir duas vezes não deve quebrar */
}

/* ---------- Inserir (push) ---------- */

static void empilhar_em_pilha_vazia(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao a = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 8);
    int seq = 0;

    VERIFICAR(pilha_empilhar(pilha, &a, &seq) == PILHA_OK);
    VERIFICAR(seq == 1);
    VERIFICAR(pilha->topo != NULL);
    VERIFICAR(pilha->topo->proximo == NULL); /* primeiro nó é a base */
    VERIFICAR(pilha->tamanho == 1);
    pilha_destruir(&pilha);
}

static void empilhar_novo_topo_aponta_para_antigo(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao a = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 8);
    Operacao b = nova_operacao(OP_REQUISICAO_RECEBIDA, "REQ-1", 2);
    NoOperacao *topoAnterior;
    int seq = 0;

    pilha_empilhar(pilha, &a, NULL);
    topoAnterior = pilha->topo;
    VERIFICAR(pilha_empilhar(pilha, &b, &seq) == PILHA_OK);
    VERIFICAR(seq == 2);
    VERIFICAR(pilha->topo->operacao.tipo == OP_REQUISICAO_RECEBIDA);
    VERIFICAR(pilha->topo->proximo == topoAnterior);
    VERIFICAR(pilha->tamanho == 2);
    pilha_destruir(&pilha);
}

static void empilhar_dados_invalidos_e_rejeitado(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao semReferencia = nova_operacao(OP_ENTRADA_ESTOQUE, "", 1);
    Operacao qtdNegativa = nova_operacao(OP_RETIRADA_ESTOQUE, "HM-0001", -2);
    Operacao dataRuim = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 1);
    Operacao tipoRuim = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 1);
    strcpy(dataRuim.data, "23/09/2026");
    tipoRuim.tipo = (TipoOperacao) 42;

    VERIFICAR(pilha_empilhar(pilha, &semReferencia, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_empilhar(pilha, &qtdNegativa, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_empilhar(pilha, &dataRuim, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_empilhar(pilha, &tipoRuim, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_empilhar(pilha, NULL, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_empilhar(NULL, &semReferencia, NULL) == PILHA_PARAMETRO_INVALIDO);
    VERIFICAR(pilha_vazia(pilha));
    VERIFICAR(pilha->proximaSequencia == 1); /* rejeições não consomem sequência */
    pilha_destruir(&pilha);
}

/* ---------- Remover (pop) ---------- */

static void desempilhar_em_ordem_lifo(void) {
    PilhaHistorico *pilha = pilha_com_tres();
    Operacao saida;

    VERIFICAR(pilha_desempilhar(pilha, &saida) == PILHA_OK);
    VERIFICAR(saida.sequencia == 3);
    VERIFICAR(saida.tipo == OP_REQUISICAO_ATENDIDA);
    VERIFICAR(pilha_desempilhar(pilha, &saida) == PILHA_OK);
    VERIFICAR(saida.sequencia == 2);
    VERIFICAR(strcmp(saida.referencia, "PL-0001") == 0);
    VERIFICAR(pilha_desempilhar(pilha, &saida) == PILHA_OK);
    VERIFICAR(saida.sequencia == 1);
    pilha_destruir(&pilha);
}

static void desempilhar_ultimo_esvazia_pilha(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao a = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 8);
    pilha_empilhar(pilha, &a, NULL);

    VERIFICAR(pilha_desempilhar(pilha, NULL) == PILHA_OK);
    VERIFICAR(pilha->topo == NULL);
    VERIFICAR(pilha->tamanho == 0);
    pilha_destruir(&pilha);
}

static void desempilhar_pilha_vazia_retorna_erro(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao saida;
    VERIFICAR(pilha_desempilhar(pilha, &saida) == PILHA_VAZIA);
    VERIFICAR(pilha_desempilhar(NULL, &saida) == PILHA_PARAMETRO_INVALIDO);
    pilha_destruir(&pilha);
}

static void reutilizar_pilha_apos_esvaziar(void) {
    PilhaHistorico *pilha = pilha_criar();
    Operacao a = nova_operacao(OP_ENTRADA_ESTOQUE, "HM-0001", 8);

    pilha_empilhar(pilha, &a, NULL);
    pilha_desempilhar(pilha, NULL);
    VERIFICAR(pilha_empilhar(pilha, &a, NULL) == PILHA_OK);
    VERIFICAR(pilha_topo(pilha)->sequencia == 2); /* sequência não é reaproveitada */
    pilha_destruir(&pilha);
}

/* ---------- Consultar ---------- */

static void consultar_topo_sem_remover(void) {
    PilhaHistorico *pilha = pilha_com_tres();
    PilhaHistorico *vazia = pilha_criar();
    const Operacao *topo = pilha_topo(pilha);

    VERIFICAR(topo != NULL);
    VERIFICAR(topo->tipo == OP_REQUISICAO_ATENDIDA);
    VERIFICAR(pilha->tamanho == 3);
    VERIFICAR(pilha_topo(vazia) == NULL);
    pilha_destruir(&pilha);
    pilha_destruir(&vazia);
}

static void consultar_quantidade_por_tipo(void) {
    PilhaHistorico *pilha = pilha_com_tres();

    VERIFICAR(pilha_contar_por_tipo(pilha, OP_ENTRADA_ESTOQUE) == 2);
    VERIFICAR(pilha_contar_por_tipo(pilha, OP_REQUISICAO_ATENDIDA) == 1);
    VERIFICAR(pilha_contar_por_tipo(pilha, OP_REMOCAO_ESTOQUE) == 0);
    pilha_desempilhar(pilha, NULL);
    VERIFICAR(pilha_contar_por_tipo(pilha, OP_REQUISICAO_ATENDIDA) == 0);
    pilha_destruir(&pilha);
}

int main(void) {
    teste_titulo("Testes: Historico de operacoes (pilha)");

    teste_secao("Criar / destruir");
    RODAR(criar_pilha_vazia, "Criar pilha vazia");
    RODAR(destruir_zera_ponteiro, "Destruir libera os nos e zera o ponteiro");

    teste_secao("Inserir (push)");
    RODAR(empilhar_em_pilha_vazia, "Empilhar em pilha vazia");
    RODAR(empilhar_novo_topo_aponta_para_antigo, "Empilhar: novo topo aponta para o antigo");
    RODAR(empilhar_dados_invalidos_e_rejeitado, "Empilhar dados invalidos e rejeitado");

    teste_secao("Remover (pop)");
    RODAR(desempilhar_em_ordem_lifo, "Desempilhar em ordem inversa (LIFO)");
    RODAR(desempilhar_ultimo_esvazia_pilha, "Desempilhar o ultimo esvazia a pilha");
    RODAR(desempilhar_pilha_vazia_retorna_erro, "Desempilhar pilha vazia retorna erro");
    RODAR(reutilizar_pilha_apos_esvaziar, "Reutilizar a pilha depois de esvaziar");

    teste_secao("Consultar");
    RODAR(consultar_topo_sem_remover, "Consultar o topo sem remover");
    RODAR(consultar_quantidade_por_tipo, "Consultar quantidade por tipo de operacao");

    return teste_resumo();
}
