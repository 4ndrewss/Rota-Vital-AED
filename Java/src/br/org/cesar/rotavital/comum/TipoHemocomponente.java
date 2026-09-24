package br.org.cesar.rotavital.comum;

/** Hemocomponentes controlados pelo sistema (equivalente ao enum de comum.h). */
public enum TipoHemocomponente {
    HEMACIAS("Hemacias"),
    PLASMA("Plasma"),
    PLAQUETAS("Plaquetas"),
    CRIOPRECIPITADO("Crioprecipitado");

    private final String nome;

    TipoHemocomponente(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
