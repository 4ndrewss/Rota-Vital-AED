#include "fila.h"
#include "teste.h"

#include <string.h>

static Requisicao nova_requisicao(const char *hospital, const char *tipo,
                                  TipoHemocomponente componente, int quantidade) {
    Requisicao requisicao;
    memset(&requisicao, 0, sizeof(requisicao));
    strncpy(requisicao.hospital, hospital, TAM_HOSPITAL - 1);
    strncpy(requisicao.responsavel, "Dra. Plantonista", TAM_RESPONSAVEL - 1);
    strncpy(requisicao.tipoSanguineo, tipo, TAM_TIPO_SANGUINEO - 1);
    requisicao.componente = componente;
    requisicao.quantidade = quantidade;
    strcpy(requisicao.data, "2026-09-23");
    return requisicao;
}

static FilaRequisicoes *fila_com_tres(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao a = nova_requisicao("IMIP", "O-", HEMACIAS, 2);
    Requisicao b = nova_requisicao("Hospital da Restauracao", "A+", PLASMA, 1);
    Requisicao c = nova_requisicao("IMIP", "B-", PLAQUETAS, 3);
    fila_enfileirar(fila, &a, NULL);
    fila_enfileirar(fila, &b, NULL);
    fila_enfileirar(fila, &c, NULL);
    return fila;
}

/* ---------- Criar / destruir ---------- */

static void criar_fila_vazia(void) {
    FilaRequisicoes *fila = fila_criar();
    VERIFICAR(fila != NULL);
    VERIFICAR(fila->inicio == NULL);
    VERIFICAR(fila->fim == NULL);
    VERIFICAR(fila->tamanho == 0);
    VERIFICAR(fila_vazia(fila));
    fila_destruir(&fila);
}

static void destruir_zera_ponteiro(void) {
    FilaRequisicoes *fila = fila_com_tres();
    fila_destruir(&fila);
    VERIFICAR(fila == NULL);
    fila_destruir(&fila); /* destruir duas vezes não deve quebrar */
}

/* ---------- Inserir (enqueue) ---------- */

static void enfileirar_em_fila_vazia(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao a = nova_requisicao("IMIP", "O-", HEMACIAS, 2);
    int id = 0;

    VERIFICAR(fila_enfileirar(fila, &a, &id) == FILA_OK);
    VERIFICAR(id == 1);
    VERIFICAR(fila->inicio != NULL);
    VERIFICAR(fila->inicio == fila->fim); /* um único nó: início e fim coincidem */
    VERIFICAR(fila->fim->proximo == NULL);
    VERIFICAR(fila->tamanho == 1);
    fila_destruir(&fila);
}

static void enfileirar_avanca_so_o_fim(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao a = nova_requisicao("IMIP", "O-", HEMACIAS, 2);
    Requisicao b = nova_requisicao("IMIP", "A+", PLASMA, 1);
    NoRequisicao *inicioOriginal;
    int id = 0;

    fila_enfileirar(fila, &a, NULL);
    inicioOriginal = fila->inicio;
    VERIFICAR(fila_enfileirar(fila, &b, &id) == FILA_OK);
    VERIFICAR(id == 2);
    VERIFICAR(fila->inicio == inicioOriginal); /* início não muda */
    VERIFICAR(fila->fim->requisicao.id == 2);
    VERIFICAR(fila->inicio->proximo == fila->fim);
    VERIFICAR(fila->fim->proximo == NULL);
    fila_destruir(&fila);
}

static void enfileirar_dados_invalidos_e_rejeitado(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao semHospital = nova_requisicao("", "O+", HEMACIAS, 1);
    Requisicao tipoRuim = nova_requisicao("IMIP", "X", HEMACIAS, 1);
    Requisicao qtdNegativa = nova_requisicao("IMIP", "O+", HEMACIAS, -1);
    Requisicao dataRuim = nova_requisicao("IMIP", "O+", HEMACIAS, 1);
    Requisicao semResponsavel = nova_requisicao("IMIP", "O+", HEMACIAS, 1);
    strcpy(dataRuim.data, "2026-13-01");
    semResponsavel.responsavel[0] = '\0';

    VERIFICAR(fila_enfileirar(fila, &semHospital, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(fila, &tipoRuim, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(fila, &qtdNegativa, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(fila, &dataRuim, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(fila, &semResponsavel, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(fila, NULL, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_enfileirar(NULL, &tipoRuim, NULL) == FILA_PARAMETRO_INVALIDO);
    VERIFICAR(fila_vazia(fila));
    VERIFICAR(fila->proximoId == 1); /* rejeições não consomem id */
    fila_destruir(&fila);
}

/* ---------- Remover (dequeue) ---------- */

static void desenfileirar_em_ordem_fifo(void) {
    FilaRequisicoes *fila = fila_com_tres();
    Requisicao saida;

    VERIFICAR(fila_desenfileirar(fila, &saida) == FILA_OK);
    VERIFICAR(saida.id == 1);
    VERIFICAR(strcmp(saida.hospital, "IMIP") == 0);
    VERIFICAR(fila_desenfileirar(fila, &saida) == FILA_OK);
    VERIFICAR(saida.id == 2);
    VERIFICAR(fila_desenfileirar(fila, &saida) == FILA_OK);
    VERIFICAR(saida.id == 3);
    fila_destruir(&fila);
}

static void desenfileirar_ultimo_zera_inicio_e_fim(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao a = nova_requisicao("IMIP", "O-", HEMACIAS, 2);
    fila_enfileirar(fila, &a, NULL);

    VERIFICAR(fila_desenfileirar(fila, NULL) == FILA_OK);
    VERIFICAR(fila->inicio == NULL);
    VERIFICAR(fila->fim == NULL); /* fim não pode apontar para o nó liberado */
    VERIFICAR(fila->tamanho == 0);
    fila_destruir(&fila);
}

static void desenfileirar_fila_vazia_retorna_erro(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao saida;
    VERIFICAR(fila_desenfileirar(fila, &saida) == FILA_VAZIA);
    VERIFICAR(fila_desenfileirar(NULL, &saida) == FILA_PARAMETRO_INVALIDO);
    fila_destruir(&fila);
}

static void reutilizar_fila_apos_esvaziar(void) {
    FilaRequisicoes *fila = fila_criar();
    Requisicao a = nova_requisicao("IMIP", "AB-", CRIOPRECIPITADO, 1);
    int id = 0;

    fila_enfileirar(fila, &a, NULL);
    fila_desenfileirar(fila, NULL);
    VERIFICAR(fila_enfileirar(fila, &a, &id) == FILA_OK);
    VERIFICAR(id == 2); /* ids não são reaproveitados */
    VERIFICAR(fila->inicio == fila->fim);
    VERIFICAR(fila->inicio != NULL);
    fila_destruir(&fila);
}

/* ---------- Consultar ---------- */

static void consultar_frente_sem_remover(void) {
    FilaRequisicoes *fila = fila_com_tres();
    FilaRequisicoes *vazia = fila_criar();
    const Requisicao *r = fila_frente(fila);

    VERIFICAR(r != NULL);
    VERIFICAR(r->id == 1);
    VERIFICAR(fila->tamanho == 3);
    VERIFICAR(fila_frente(vazia) == NULL);
    fila_destruir(&fila);
    fila_destruir(&vazia);
}

static void consultar_por_id(void) {
    FilaRequisicoes *fila = fila_com_tres();
    const Requisicao *r = fila_buscar(fila, 3);

    VERIFICAR(r != NULL);
    VERIFICAR(strcmp(r->tipoSanguineo, "B-") == 0);
    VERIFICAR(fila_buscar(fila, 99) == NULL);
    fila_destruir(&fila);
}

static void consultar_posicao_na_fila(void) {
    FilaRequisicoes *fila = fila_com_tres();

    VERIFICAR(fila_posicao(fila, 1) == 1);
    VERIFICAR(fila_posicao(fila, 3) == 3);
    VERIFICAR(fila_posicao(fila, 99) == 0);
    fila_desenfileirar(fila, NULL);
    VERIFICAR(fila_posicao(fila, 3) == 2); /* todos avançam uma posição */
    fila_destruir(&fila);
}

static void consultar_pendencias_por_hospital(void) {
    FilaRequisicoes *fila = fila_com_tres();

    VERIFICAR(fila_contar_por_hospital(fila, "IMIP") == 2);
    VERIFICAR(fila_contar_por_hospital(fila, "Hospital Inexistente") == 0);
    fila_desenfileirar(fila, NULL);
    VERIFICAR(fila_contar_por_hospital(fila, "IMIP") == 1);
    fila_destruir(&fila);
}

int main(void) {
    teste_titulo("Testes: Requisicoes hospitalares (fila)");

    teste_secao("Criar / destruir");
    RODAR(criar_fila_vazia, "Criar fila vazia");
    RODAR(destruir_zera_ponteiro, "Destruir libera os nos e zera o ponteiro");

    teste_secao("Inserir (enqueue)");
    RODAR(enfileirar_em_fila_vazia, "Enfileirar em fila vazia (inicio == fim)");
    RODAR(enfileirar_avanca_so_o_fim, "Enfileirar avanca so o fim");
    RODAR(enfileirar_dados_invalidos_e_rejeitado, "Enfileirar dados invalidos e rejeitado");

    teste_secao("Remover (dequeue)");
    RODAR(desenfileirar_em_ordem_fifo, "Desenfileirar em ordem de chegada (FIFO)");
    RODAR(desenfileirar_ultimo_zera_inicio_e_fim, "Desenfileirar o ultimo zera inicio e fim");
    RODAR(desenfileirar_fila_vazia_retorna_erro, "Desenfileirar fila vazia retorna erro");
    RODAR(reutilizar_fila_apos_esvaziar, "Reutilizar a fila depois de esvaziar");

    teste_secao("Consultar");
    RODAR(consultar_frente_sem_remover, "Consultar a frente sem remover");
    RODAR(consultar_por_id, "Consultar por id");
    RODAR(consultar_posicao_na_fila, "Consultar posicao na fila");
    RODAR(consultar_pendencias_por_hospital, "Consultar pendencias por hospital");

    return teste_resumo();
}
