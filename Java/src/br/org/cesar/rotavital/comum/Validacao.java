package br.org.cesar.rotavital.comum;

import java.util.Set;

/** Validações compartilhadas entre as estruturas (equivalente a comum.c). */
public final class Validacao {

    private static final Set<String> TIPOS_SANGUINEOS =
            Set.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private Validacao() {
    }

    /** A+, A-, B+, B-, AB+, AB-, O+, O- */
    public static boolean tipoSanguineoValido(String tipo) {
        return tipo != null && TIPOS_SANGUINEOS.contains(tipo);
    }

    /** Aceita apenas AAAA-MM-DD, mesmo formato usado na versão em C. */
    public static boolean dataValida(String data) {
        if (data == null || !data.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        int mes = Integer.parseInt(data.substring(5, 7));
        int dia = Integer.parseInt(data.substring(8, 10));
        return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
    }

    public static boolean textoPreenchido(String texto, int tamanhoMaximo) {
        return texto != null && !texto.isEmpty() && texto.length() <= tamanhoMaximo;
    }
}
