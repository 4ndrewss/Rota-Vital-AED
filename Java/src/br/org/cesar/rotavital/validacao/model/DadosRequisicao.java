package br.org.cesar.rotavital.validacao.model;

/**
 * Modelo de dados que encapsula os metadados extraídos de uma requisição de hemocomponentes.
 */
public class DadosRequisicao {
    private String data;
    private String prefixoAgencia;
    private String nomeResponsavel;
    private Boolean assinaturaPresente;

    public DadosRequisicao() {}

    public DadosRequisicao(String data, String prefixoAgencia, String nomeResponsavel, Boolean assinaturaPresente) {
        this.data = data;
        this.prefixoAgencia = prefixoAgencia;
        this.nomeResponsavel = nomeResponsavel;
        this.assinaturaPresente = assinaturaPresente;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getPrefixoAgencia() {
        return prefixoAgencia;
    }

    public void setPrefixoAgencia(String prefixoAgencia) {
        this.prefixoAgencia = prefixoAgencia;
    }

    public String getNomeResponsavel() {
        return nomeResponsavel;
    }

    public void setNomeResponsavel(String nomeResponsavel) {
        this.nomeResponsavel = nomeResponsavel;
    }

    public Boolean getAssinaturaPresente() {
        return assinaturaPresente;
    }

    public void setAssinaturaPresente(Boolean assinaturaPresente) {
        this.assinaturaPresente = assinaturaPresente;
    }

    @Override
    public String toString() {
        return "DadosRequisicao{" +
                "data='" + data + '\'' +
                ", prefixoAgencia='" + prefixoAgencia + '\'' +
                ", nomeResponsavel='" + nomeResponsavel + '\'' +
                ", assinaturaPresente=" + assinaturaPresente +
                '}';
    }
}
