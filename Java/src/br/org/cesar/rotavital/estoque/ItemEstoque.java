package br.org.cesar.rotavital.estoque;

import br.org.cesar.rotavital.comum.TipoHemocomponente;

/**
 * Lote de hemocomponente no estoque (equivalente ao struct ItemEstoque de estoque.h).
 *
 * POJO com construtor vazio e getters/setters para poder ser usado direto como
 * JSON quando o projeto migrar para Spring Boot.
 */
public class ItemEstoque {

    public static final int TAM_CODIGO = 15;

    private String codigo;              /* identificador único do lote */
    private String tipoSanguineo;       /* A+, A-, B+, B-, AB+, AB-, O+, O- */
    private TipoHemocomponente componente;
    private int quantidade;             /* número de bolsas */
    private String validade;            /* AAAA-MM-DD */

    public ItemEstoque() {
    }

    public ItemEstoque(String codigo, String tipoSanguineo, TipoHemocomponente componente,
                       int quantidade, String validade) {
        this.codigo = codigo;
        this.tipoSanguineo = tipoSanguineo;
        this.componente = componente;
        this.quantidade = quantidade;
        this.validade = validade;
    }

    /** Cópia, para o estoque não depender de objetos que o chamador ainda pode alterar. */
    public ItemEstoque(ItemEstoque outro) {
        this(outro.codigo, outro.tipoSanguineo, outro.componente, outro.quantidade, outro.validade);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipoSanguineo() {
        return tipoSanguineo;
    }

    public void setTipoSanguineo(String tipoSanguineo) {
        this.tipoSanguineo = tipoSanguineo;
    }

    public TipoHemocomponente getComponente() {
        return componente;
    }

    public void setComponente(TipoHemocomponente componente) {
        this.componente = componente;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getValidade() {
        return validade;
    }

    public void setValidade(String validade) {
        this.validade = validade;
    }

    @Override
    public String toString() {
        return "ItemEstoque{" +
                "codigo='" + codigo + '\'' +
                ", tipoSanguineo='" + tipoSanguineo + '\'' +
                ", componente=" + componente +
                ", quantidade=" + quantidade +
                ", validade='" + validade + '\'' +
                '}';
    }
}
