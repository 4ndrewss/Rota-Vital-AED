package br.org.cesar.rotavital.historico;

/** Lançada ao desempilhar um histórico sem operações (equivalente a PILHA_VAZIA em C). */
public class PilhaVaziaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PilhaVaziaException() {
        super("Historico vazio");
    }
}
