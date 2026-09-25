package br.org.cesar.rotavital.historico;

import br.org.cesar.rotavital.comum.Validacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Histórico de operações — pilha implementada com lista encadeada
 * (equivalente a pilha.h / pilha.c).
 *
 * Política LIFO: a operação mais recente fica no `topo`. Push e pop
 * acontecem no topo, ambos em O(1), o que permite consultar ou desfazer
 * a última operação realizada.
 *
 * Não usa java.util.Stack/Deque: os nós são implementados à mão, como na versão em C.
 */
public class PilhaHistorico {

    private static class NoOperacao {
        final Operacao operacao;
        NoOperacao proximo;   /* aponta para a operação anterior */

        NoOperacao(Operacao operacao) {
            this.operacao = operacao;
        }
    }

    private NoOperacao topo;   /* operação mais recente */
    private int tamanho;
    private int proximaSequencia = 1;

    /**
     * Push: empilha uma cópia da operação e retorna a sequência atribuída.
     *
     * @throws IllegalArgumentException se algum campo for inválido
     */
    public int empilhar(Operacao operacao) {
        validar(operacao);

        NoOperacao novo = new NoOperacao(new Operacao(operacao));
        novo.operacao.setSequencia(proximaSequencia++);

        /* O novo nó aponta para o antigo topo e passa a ser o topo. */
        novo.proximo = topo;
        topo = novo;
        tamanho++;
        return novo.operacao.getSequencia();
    }

    /**
     * Pop: remove e retorna a operação do topo.
     *
     * @throws PilhaVaziaException se o histórico estiver vazio
     */
    public Operacao desempilhar() {
        if (topo == null) {
            throw new PilhaVaziaException();
        }
        NoOperacao removido = topo;
        topo = removido.proximo;
        tamanho--;
        return removido.operacao;
    }

    /** Retorna (uma cópia de) a operação do topo sem removê-la, ou null se vazia. */
    public Operacao topo() {
        return topo == null ? null : new Operacao(topo.operacao);
    }

    /** Quantas operações do tipo informado existem no histórico. */
    public int contarPorTipo(TipoOperacao tipo) {
        int total = 0;
        for (NoOperacao atual = topo; atual != null; atual = atual.proximo) {
            if (atual.operacao.getTipo() == tipo) {
                total++;
            }
        }
        return total;
    }

    /** Cópia das operações do topo para a base (útil para expor como JSON). */
    public List<Operacao> listar() {
        List<Operacao> lista = new ArrayList<>(tamanho);
        for (NoOperacao atual = topo; atual != null; atual = atual.proximo) {
            lista.add(new Operacao(atual.operacao));
        }
        return lista;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return topo == null;
    }

    /** Imprime do topo para a base (mais recente primeiro). */
    public void imprimir() {
        System.out.printf("Historico de operacoes (%d registro(s), mais recente primeiro):%n", tamanho);
        if (topo == null) {
            System.out.println("  (vazio)");
            return;
        }
        System.out.printf("  %-4s %-20s %-10s %4s  %-10s  %s%n",
                "SEQ", "OPERACAO", "REF", "QTD", "DATA", "DESCRICAO");
        for (NoOperacao atual = topo; atual != null; atual = atual.proximo) {
            Operacao o = atual.operacao;
            System.out.printf("  %-4d %-20s %-10s %4d  %-10s  %s%n",
                    o.getSequencia(), o.getTipo().getNome(), o.getReferencia(),
                    o.getQuantidade(), o.getData(), o.getDescricao());
        }
    }

    private static void validar(Operacao operacao) {
        if (operacao == null) {
            throw new IllegalArgumentException("Operacao nula");
        }
        if (operacao.getTipo() == null) {
            throw new IllegalArgumentException("Tipo de operacao invalido");
        }
        if (!Validacao.textoPreenchido(operacao.getReferencia(), Operacao.TAM_REFERENCIA)) {
            throw new IllegalArgumentException("Referencia invalida");
        }
        if (operacao.getDescricao() != null && operacao.getDescricao().length() > Operacao.TAM_DESCRICAO) {
            throw new IllegalArgumentException("Descricao muito longa");
        }
        if (operacao.getQuantidade() < 0) {
            throw new IllegalArgumentException("Quantidade nao pode ser negativa");
        }
        if (!Validacao.dataValida(operacao.getData())) {
            throw new IllegalArgumentException("Data invalida (use AAAA-MM-DD)");
        }
    }
}
