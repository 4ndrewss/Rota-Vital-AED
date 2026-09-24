#include "estoque.h"
#include "teste.h"

#include <string.h>

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

/* ---------- Criar / destruir ---------- */

static void criar_estoque_vazio(void) {
    Estoque *estoque = estoque_criar();
    VERIFICAR(estoque != NULL);
    VERIFICAR(estoque->inicio == NULL);
    VERIFICAR(estoque->tamanho == 0);
    estoque_destruir(&estoque);
}

static void destruir_zera_ponteiro(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O+", HEMACIAS, 1, "2026-12-01");
    estoque_inserir(estoque, &a);
    estoque_destruir(&estoque);
    VERIFICAR(estoque == NULL);
    estoque_destruir(&estoque); /* destruir duas vezes não deve quebrar */
}

/* ---------- Inserir ---------- */

static void inserir_em_lista_vazia(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O-", HEMACIAS, 5, "2026-12-01");

    VERIFICAR(estoque_inserir(estoque, &a) == ESTOQUE_OK);
    VERIFICAR(estoque->tamanho == 1);
    VERIFICAR(estoque->inicio != NULL);
    VERIFICAR(strcmp(estoque->inicio->item.codigo, "L001") == 0);
    VERIFICAR(estoque->inicio->proximo == NULL);
    estoque_destruir(&estoque);
}

static void inserir_no_fim_mantem_ordem_de_cadastro(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O-", HEMACIAS, 5, "2026-12-01");
    ItemEstoque b = novo_item("L002", "A+", PLASMA, 3, "2026-10-15");
    ItemEstoque c = novo_item("L003", "B+", PLAQUETAS, 2, "2027-01-20");
    ItemEstoque d = novo_item("L004", "AB-", HEMACIAS, 1, "2026-11-10");
    NoEstoque *no;

    VERIFICAR(estoque_inserir(estoque, &a) == ESTOQUE_OK);
    VERIFICAR(estoque_inserir(estoque, &b) == ESTOQUE_OK);
    VERIFICAR(estoque_inserir(estoque, &c) == ESTOQUE_OK);
    VERIFICAR(estoque_inserir(estoque, &d) == ESTOQUE_OK);
    VERIFICAR(estoque->tamanho == 4);

    no = estoque->inicio;
    VERIFICAR(strcmp(no->item.codigo, "L001") == 0);
    no = no->proximo;
    VERIFICAR(strcmp(no->item.codigo, "L002") == 0);
    no = no->proximo;
    VERIFICAR(strcmp(no->item.codigo, "L003") == 0);
    no = no->proximo;
    VERIFICAR(strcmp(no->item.codigo, "L004") == 0);
    VERIFICAR(no->proximo == NULL);
    estoque_destruir(&estoque);
}

static void inserir_codigo_duplicado_e_rejeitado(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O+", HEMACIAS, 2, "2026-12-01");
    ItemEstoque b = novo_item("L001", "A-", PLASMA, 1, "2026-12-05");

    VERIFICAR(estoque_inserir(estoque, &a) == ESTOQUE_OK);
    VERIFICAR(estoque_inserir(estoque, &b) == ESTOQUE_CODIGO_DUPLICADO);
    VERIFICAR(estoque->tamanho == 1);
    estoque_destruir(&estoque);
}

static void inserir_dados_invalidos_e_rejeitado(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque tipoRuim = novo_item("L002", "C+", HEMACIAS, 1, "2026-12-01");
    ItemEstoque qtdZero = novo_item("L003", "O+", HEMACIAS, 0, "2026-12-01");
    ItemEstoque dataRuim = novo_item("L004", "O+", HEMACIAS, 1, "01/12/2026");
    ItemEstoque codigoVazio = novo_item("", "O+", HEMACIAS, 1, "2026-12-01");

    VERIFICAR(estoque_inserir(estoque, &tipoRuim) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_inserir(estoque, &qtdZero) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_inserir(estoque, &dataRuim) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_inserir(estoque, &codigoVazio) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_inserir(estoque, NULL) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_inserir(NULL, &tipoRuim) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque->tamanho == 0);
    estoque_destruir(&estoque);
}

/* ---------- Remover ---------- */

static Estoque *estoque_com_tres_itens(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O+", HEMACIAS, 1, "2026-10-01");
    ItemEstoque b = novo_item("L002", "O+", HEMACIAS, 1, "2026-11-01");
    ItemEstoque c = novo_item("L003", "O+", HEMACIAS, 1, "2026-12-01");
    estoque_inserir(estoque, &a);
    estoque_inserir(estoque, &b);
    estoque_inserir(estoque, &c);
    return estoque;
}

static void remover_do_inicio(void) {
    Estoque *estoque = estoque_com_tres_itens();
    VERIFICAR(estoque_remover(estoque, "L001") == ESTOQUE_OK);
    VERIFICAR(strcmp(estoque->inicio->item.codigo, "L002") == 0);
    VERIFICAR(estoque->tamanho == 2);
    estoque_destruir(&estoque);
}

static void remover_do_meio(void) {
    Estoque *estoque = estoque_com_tres_itens();
    VERIFICAR(estoque_remover(estoque, "L002") == ESTOQUE_OK);
    VERIFICAR(estoque->inicio->proximo == estoque_buscar(estoque, "L003")); /* religou os vizinhos */
    VERIFICAR(estoque->tamanho == 2);
    estoque_destruir(&estoque);
}

static void remover_do_fim(void) {
    Estoque *estoque = estoque_com_tres_itens();
    VERIFICAR(estoque_remover(estoque, "L003") == ESTOQUE_OK);
    VERIFICAR(estoque->inicio->proximo->proximo == NULL);
    VERIFICAR(estoque->tamanho == 2);
    estoque_destruir(&estoque);
}

static void remover_ultimo_item_esvazia_lista(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O+", HEMACIAS, 1, "2026-10-01");
    estoque_inserir(estoque, &a);
    VERIFICAR(estoque_remover(estoque, "L001") == ESTOQUE_OK);
    VERIFICAR(estoque->inicio == NULL);
    VERIFICAR(estoque->tamanho == 0);
    estoque_destruir(&estoque);
}

static void remover_inexistente_retorna_erro(void) {
    Estoque *estoque = estoque_com_tres_itens();
    Estoque *vazio = estoque_criar();
    VERIFICAR(estoque_remover(estoque, "L999") == ESTOQUE_NAO_ENCONTRADO);
    VERIFICAR(estoque->tamanho == 3);
    VERIFICAR(estoque_remover(vazio, "L001") == ESTOQUE_NAO_ENCONTRADO);
    estoque_destruir(&estoque);
    estoque_destruir(&vazio);
}

static void retirar_parcial_e_total(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O+", HEMACIAS, 5, "2026-12-01");
    estoque_inserir(estoque, &a);

    VERIFICAR(estoque_retirar(estoque, "L001", 0) == ESTOQUE_PARAMETRO_INVALIDO);
    VERIFICAR(estoque_retirar(estoque, "L001", 6) == ESTOQUE_QUANTIDADE_INSUFICIENTE);
    VERIFICAR(estoque_retirar(estoque, "L999", 1) == ESTOQUE_NAO_ENCONTRADO);
    VERIFICAR(estoque_retirar(estoque, "L001", 2) == ESTOQUE_OK);
    VERIFICAR(estoque_buscar(estoque, "L001")->item.quantidade == 3);
    VERIFICAR(estoque_retirar(estoque, "L001", 3) == ESTOQUE_OK); /* zera e remove o nó */
    VERIFICAR(estoque_buscar(estoque, "L001") == NULL);
    VERIFICAR(estoque->tamanho == 0);
    estoque_destruir(&estoque);
}

/* ---------- Consultar ---------- */

static Estoque *estoque_para_consulta(void) {
    Estoque *estoque = estoque_criar();
    ItemEstoque a = novo_item("L001", "O-", HEMACIAS, 4, "2026-12-01");
    ItemEstoque b = novo_item("L002", "O-", HEMACIAS, 6, "2026-10-20");
    ItemEstoque c = novo_item("L003", "O-", PLASMA, 9, "2026-10-01");
    ItemEstoque d = novo_item("L004", "A+", HEMACIAS, 2, "2026-09-30");
    estoque_inserir(estoque, &a);
    estoque_inserir(estoque, &b);
    estoque_inserir(estoque, &c);
    estoque_inserir(estoque, &d);
    return estoque;
}

static void consultar_por_codigo(void) {
    Estoque *estoque = estoque_para_consulta();
    NoEstoque *no = estoque_buscar(estoque, "L003");
    VERIFICAR(no != NULL);
    VERIFICAR(no->item.quantidade == 9);
    VERIFICAR(no->item.componente == PLASMA);
    VERIFICAR(estoque_buscar(estoque, "L999") == NULL);
    estoque_destruir(&estoque);
}

static void consultar_total_por_tipo_e_componente(void) {
    Estoque *estoque = estoque_para_consulta();
    VERIFICAR(estoque_total_disponivel(estoque, "O-", HEMACIAS) == 10);
    VERIFICAR(estoque_total_disponivel(estoque, "O-", PLASMA) == 9);
    VERIFICAR(estoque_total_disponivel(estoque, "B+", HEMACIAS) == 0);
    estoque_destruir(&estoque);
}

int main(void) {
    teste_titulo("Testes: Estoque (lista encadeada)");

    teste_secao("Criar / destruir");
    RODAR(criar_estoque_vazio, "Criar estoque vazio");
    RODAR(destruir_zera_ponteiro, "Destruir libera os nos e zera o ponteiro");

    teste_secao("Inserir");
    RODAR(inserir_em_lista_vazia, "Inserir em lista vazia");
    RODAR(inserir_no_fim_mantem_ordem_de_cadastro, "Inserir no fim mantem ordem de cadastro");
    RODAR(inserir_codigo_duplicado_e_rejeitado, "Inserir codigo duplicado e rejeitado");
    RODAR(inserir_dados_invalidos_e_rejeitado, "Inserir dados invalidos e rejeitado");

    teste_secao("Remover");
    RODAR(remover_do_inicio, "Remover do inicio");
    RODAR(remover_do_meio, "Remover do meio");
    RODAR(remover_do_fim, "Remover do fim");
    RODAR(remover_ultimo_item_esvazia_lista, "Remover o unico item esvazia a lista");
    RODAR(remover_inexistente_retorna_erro, "Remover codigo inexistente retorna erro");
    RODAR(retirar_parcial_e_total, "Retirar bolsas (parcial e total)");

    teste_secao("Consultar");
    RODAR(consultar_por_codigo, "Consultar por codigo");
    RODAR(consultar_total_por_tipo_e_componente, "Consultar total por tipo sanguineo e componente");

    return teste_resumo();
}
