package br.org.cesar.rotavital.historico;

/**
 * Operação registrada no histórico (equivalente ao struct Operacao de pilha.h).
 *
 * POJO com construtor vazio e getters/setters para poder ser usado direto como
 * JSON quando o projeto migrar para Spring Boot.
 */
public class Operacao {

    public static final int TAM_REFERENCIA = 15;
    public static final int TAM_DESCRICAO = 95;

    private int sequencia;              /* atribuída pela pilha ao empilhar */
    private TipoOperacao tipo;
    private String referencia;          /* código do lote ou id da requisição */
    private int quantidade;             /* bolsas envolvidas (0 se não se aplica) */
    private String data;                /* AAAA-MM-DD */
    private String descricao = "";      /* texto livre, opcional */

    public Operacao() {
    }

    public Operacao(TipoOperacao tipo, String referencia, int quantidade, String data, String descricao) {
        this.tipo = tipo;
        this.referencia = referencia;
        this.quantidade = quantidade;
        this.data = data;
        this.descricao = descricao;
    }

    /** Cópia, para a pilha não depender de objetos que o chamador ainda pode alterar. */
    public Operacao(Operacao outra) {
        this(outra.tipo, outra.referencia, outra.quantidade, outra.data, outra.descricao);
        this.sequencia = outra.sequencia;
    }

    public int getSequencia() {
        return sequencia;
    }

    public void setSequencia(int sequencia) {
        this.sequencia = sequencia;
    }

    public TipoOperacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOperacao tipo) {
        this.tipo = tipo;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return "Operacao{" +
                "sequencia=" + sequencia +
                ", tipo=" + tipo +
                ", referencia='" + referencia + '\'' +
                ", quantidade=" + quantidade +
                ", data='" + data + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}
