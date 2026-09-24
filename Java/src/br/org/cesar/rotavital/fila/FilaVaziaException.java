package br.org.cesar.rotavital.fila;

/** Lançada ao desenfileirar uma fila sem requisições (equivalente a FILA_VAZIA em C). */
public class FilaVaziaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FilaVaziaException() {
        super("Fila vazia");
    }
}
