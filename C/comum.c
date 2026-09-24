#include "comum.h"

#include <ctype.h>
#include <string.h>

static const char *TIPOS_SANGUINEOS[] = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

int tipo_sanguineo_valido(const char *tipo) {
    size_t i;
    if (tipo == NULL) {
        return 0;
    }
    for (i = 0; i < sizeof(TIPOS_SANGUINEOS) / sizeof(TIPOS_SANGUINEOS[0]); i++) {
        if (strcmp(tipo, TIPOS_SANGUINEOS[i]) == 0) {
            return 1;
        }
    }
    return 0;
}

int data_valida(const char *data) {
    int i, mes, dia;
    if (data == NULL || strlen(data) != TAM_DATA - 1 || data[4] != '-' || data[7] != '-') {
        return 0;
    }
    for (i = 0; i < TAM_DATA - 1; i++) {
        if (i != 4 && i != 7 && !isdigit((unsigned char) data[i])) {
            return 0;
        }
    }
    mes = (data[5] - '0') * 10 + (data[6] - '0');
    dia = (data[8] - '0') * 10 + (data[9] - '0');
    return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
}

int componente_valido(TipoHemocomponente componente) {
    return componente >= HEMACIAS && componente <= CRIOPRECIPITADO;
}

const char *nome_componente(TipoHemocomponente componente) {
    switch (componente) {
        case HEMACIAS:        return "Hemacias";
        case PLASMA:          return "Plasma";
        case PLAQUETAS:       return "Plaquetas";
        case CRIOPRECIPITADO: return "Crioprecipitado";
    }
    return "Desconhecido";
}
