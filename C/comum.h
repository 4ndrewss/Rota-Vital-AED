#ifndef COMUM_H
#define COMUM_H

/* Tipos e validações compartilhados entre o estoque e a fila de requisições. */

#define TAM_TIPO_SANGUINEO 4 /* "AB+" + '\0' */
#define TAM_DATA 11          /* "AAAA-MM-DD" + '\0' */

typedef enum {
    HEMACIAS,
    PLASMA,
    PLAQUETAS,
    CRIOPRECIPITADO
} TipoHemocomponente;

/* A+, A-, B+, B-, AB+, AB-, O+, O- */
int tipo_sanguineo_valido(const char *tipo);

/* Aceita apenas AAAA-MM-DD, formato que permite comparar datas com strcmp. */
int data_valida(const char *data);

int componente_valido(TipoHemocomponente componente);

const char *nome_componente(TipoHemocomponente componente);

#endif
