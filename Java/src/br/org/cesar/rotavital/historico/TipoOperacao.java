package br.org.cesar.rotavital.historico;

/** Tipos de operação registrados no histórico (equivalente ao enum TipoOperacao de pilha.h). */
public enum TipoOperacao {
    ENTRADA_ESTOQUE("Entrada estoque"),          /* lote cadastrado no estoque */
    REMOCAO_ESTOQUE("Remocao estoque"),          /* lote removido do estoque (descarte, vencimento) */
    RETIRADA_ESTOQUE("Retirada estoque"),        /* baixa de bolsas de um lote */
    REQUISICAO_RECEBIDA("Requisicao recebida"),  /* requisição entrou na fila */
    REQUISICAO_ATENDIDA("Requisicao atendida"),
    REQUISICAO_RECUSADA("Requisicao recusada");

    private final String nome;

    TipoOperacao(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
