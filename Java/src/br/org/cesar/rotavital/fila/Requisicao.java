package br.org.cesar.rotavital.fila;

import br.org.cesar.rotavital.comum.TipoHemocomponente;

/**
 * Requisição hospitalar de hemocomponentes (equivalente ao struct Requisicao de fila.h).
 *
 * POJO com construtor vazio e getters/setters para poder ser usado direto como
 * corpo de requisição/resposta JSON quando o projeto migrar para Spring Boot.
 */
public class Requisicao {

    public static final int TAM_HOSPITAL = 63;
    public static final int TAM_RESPONSAVEL = 63;

    private int id;                     /* atribuído pela fila ao enfileirar */
    private String hospital;
    private String responsavel;
    private String tipoSanguineo;       /* A+, A-, B+, B-, AB+, AB-, O+, O- */
    private TipoHemocomponente componente;
    private int quantidade;             /* número de bolsas solicitadas */
    private String data;                /* AAAA-MM-DD */

    public Requisicao() {
    }

    public Requisicao(String hospital, String responsavel, String tipoSanguineo,
                      TipoHemocomponente componente, int quantidade, String data) {
        this.hospital = hospital;
        this.responsavel = responsavel;
        this.tipoSanguineo = tipoSanguineo;
        this.componente = componente;
        this.quantidade = quantidade;
        this.data = data;
    }

    /** Cópia, para a fila não depender de objetos que o chamador ainda pode alterar. */
    public Requisicao(Requisicao outra) {
        this(outra.hospital, outra.responsavel, outra.tipoSanguineo,
             outra.componente, outra.quantidade, outra.data);
        this.id = outra.id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Requisicao{" +
                "id=" + id +
                ", hospital='" + hospital + '\'' +
                ", responsavel='" + responsavel + '\'' +
                ", tipoSanguineo='" + tipoSanguineo + '\'' +
                ", componente=" + componente +
                ", quantidade=" + quantidade +
                ", data='" + data + '\'' +
                '}';
    }
}
