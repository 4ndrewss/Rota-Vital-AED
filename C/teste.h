#ifndef TESTE_H
#define TESTE_H

/*
 * Mini framework de testes, sem dependências.
 *
 * Cada caso é uma função `void caso(void)` executada com RODAR(caso, "descrição").
 * VERIFICAR(condição) encerra o caso na primeira falha e guarda onde falhou.
 * Ao final, teste_resumo() imprime o placar e devolve o código de saída
 * (0 = todos passaram), para o terminal/script saber se houve falha.
 *
 * Diferente de assert, continua rodando os outros casos após uma falha
 * e não é desligado por -DNDEBUG.
 */

#include <stdio.h>

static int casosExecutados = 0;
static int casosFalhos = 0;
static int casoAtualFalhou = 0;
static char detalheFalha[256];

#define VERIFICAR(condicao)                                                        \
    do {                                                                           \
        if (!(condicao)) {                                                         \
            snprintf(detalheFalha, sizeof(detalheFalha), "%s (%s:%d)",             \
                     #condicao, __FILE__, __LINE__);                               \
            casoAtualFalhou = 1;                                                   \
            return;                                                                \
        }                                                                          \
    } while (0)

static void RODAR(void (*caso)(void), const char *descricao) {
    casoAtualFalhou = 0;
    detalheFalha[0] = '\0';
    caso();
    casosExecutados++;
    if (casoAtualFalhou) {
        casosFalhos++;
        printf("  [FALHOU] %s\n           -> %s\n", descricao, detalheFalha);
    } else {
        printf("  [PASSOU] %s\n", descricao);
    }
}

static void teste_titulo(const char *titulo) {
    printf("\n=== %s ===\n", titulo);
}

static void teste_secao(const char *secao) {
    printf("\n %s\n", secao);
}

static int teste_resumo(void) {
    printf("\nResultado: %d/%d casos passaram", casosExecutados - casosFalhos, casosExecutados);
    printf(casosFalhos == 0 ? "\n" : " (%d falha(s))\n", casosFalhos);
    return casosFalhos == 0 ? 0 : 1;
}

#endif
