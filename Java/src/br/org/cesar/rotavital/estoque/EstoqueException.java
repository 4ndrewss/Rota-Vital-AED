package br.org.cesar.rotavital.estoque;

/**
 * Lançada quando uma operação no estoque não pode ser feita
 * (equivalente aos códigos de erro de ResultadoEstoque em C).
 */
public class EstoqueException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public enum Motivo {
        CODIGO_DUPLICADO("Codigo ja cadastrado no estoque"),
        NAO_ENCONTRADO("Item nao encontrado"),
        QUANTIDADE_INSUFICIENTE("Quantidade insuficiente em estoque");

        private final String mensagem;

        Motivo(String mensagem) {
            this.mensagem = mensagem;
        }

        public String getMensagem() {
            return mensagem;
        }
    }

    private final Motivo motivo;

    public EstoqueException(Motivo motivo) {
        super(motivo.getMensagem());
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
