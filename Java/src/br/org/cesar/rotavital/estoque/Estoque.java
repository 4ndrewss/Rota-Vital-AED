package br.org.cesar.rotavital.estoque;

import br.org.cesar.rotavital.comum.TipoHemocomponente;
import br.org.cesar.rotavital.comum.Validacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Estoque de hemocomponentes — lista simplesmente encadeada
 * (equivalente a estoque.h / estoque.c).
 *
 * Novos itens entram no fim da lista, mantendo a ordem de cadastro.
 *
 * Não usa java.util.LinkedList: os nós são implementados à mão, como na versão em C.
 */
public class Estoque {

    private static class NoEstoque {
        final ItemEstoque item;
        NoEstoque proximo;

        NoEstoque(ItemEstoque item) {
            this.item = item;
        }
    }

    private NoEstoque inicio;
    private int tamanho;

    /**
     * Insere uma cópia do item no fim da lista.
     *
     * @throws IllegalArgumentException se algum campo for inválido
     * @throws EstoqueException         se o código já estiver cadastrado
     */
    public void inserir(ItemEstoque item) {
        validar(item);
        if (buscarNo(item.getCodigo()) != null) {
            throw new EstoqueException(EstoqueException.Motivo.CODIGO_DUPLICADO);
        }

        NoEstoque novo = new NoEstoque(new ItemEstoque(item));
        if (inicio == null) {
            inicio = novo; /* lista vazia: novo nó vira a cabeça */
        } else {
            NoEstoque ultimo = inicio;
            while (ultimo.proximo != null) {
                ultimo = ultimo.proximo;
            }
            ultimo.proximo = novo;
        }
        tamanho++;
    }

    /**
     * Remove o item com o código informado.
     *
     * @throws EstoqueException se o código não existir
     */
    public void remover(String codigo) {
        NoEstoque anterior = null;
        NoEstoque atual = inicio;
        while (atual != null && !atual.item.getCodigo().equals(codigo)) {
            anterior = atual;
            atual = atual.proximo;
        }
        if (atual == null) {
            throw new EstoqueException(EstoqueException.Motivo.NAO_ENCONTRADO);
        }

        if (anterior == null) {
            inicio = atual.proximo;
        } else {
            anterior.proximo = atual.proximo;
        }
        tamanho--;
    }

    /**
     * Dá baixa em `quantidade` bolsas do item; remove o nó se zerar.
     *
     * @throws IllegalArgumentException se a quantidade não for positiva
     * @throws EstoqueException         se o código não existir ou não houver bolsas suficientes
     */
    public void retirar(String codigo, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        NoEstoque no = buscarNo(codigo);
        if (no == null) {
            throw new EstoqueException(EstoqueException.Motivo.NAO_ENCONTRADO);
        }
        if (no.item.getQuantidade() < quantidade) {
            throw new EstoqueException(EstoqueException.Motivo.QUANTIDADE_INSUFICIENTE);
        }

        no.item.setQuantidade(no.item.getQuantidade() - quantidade);
        if (no.item.getQuantidade() == 0) {
            remover(codigo);
        }
    }

    /** Retorna (uma cópia de) o item com o código informado, ou null. */
    public ItemEstoque buscar(String codigo) {
        NoEstoque no = buscarNo(codigo);
        return no == null ? null : new ItemEstoque(no.item);
    }

    /** Soma as bolsas disponíveis de um tipo sanguíneo + hemocomponente. */
    public int totalDisponivel(String tipoSanguineo, TipoHemocomponente componente) {
        int total = 0;
        if (tipoSanguineo == null) {
            return 0;
        }
        for (NoEstoque atual = inicio; atual != null; atual = atual.proximo) {
            if (atual.item.getComponente() == componente
                    && tipoSanguineo.equals(atual.item.getTipoSanguineo())) {
                total += atual.item.getQuantidade();
            }
        }
        return total;
    }

    /** Cópia dos itens na ordem da lista (útil para expor como JSON). */
    public List<ItemEstoque> listar() {
        List<ItemEstoque> lista = new ArrayList<>(tamanho);
        for (NoEstoque atual = inicio; atual != null; atual = atual.proximo) {
            lista.add(new ItemEstoque(atual.item));
        }
        return lista;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean vazio() {
        return inicio == null;
    }

    public void imprimir() {
        System.out.printf("Estoque (%d item(ns)):%n", tamanho);
        if (inicio == null) {
            System.out.println("  (vazio)");
            return;
        }
        System.out.printf("  %-15s %-4s %-16s %5s  %s%n", "CODIGO", "TIPO", "COMPONENTE", "QTD", "VALIDADE");
        for (NoEstoque atual = inicio; atual != null; atual = atual.proximo) {
            ItemEstoque i = atual.item;
            System.out.printf("  %-15s %-4s %-16s %5d  %s%n",
                    i.getCodigo(), i.getTipoSanguineo(), i.getComponente().getNome(),
                    i.getQuantidade(), i.getValidade());
        }
    }

    private NoEstoque buscarNo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (NoEstoque atual = inicio; atual != null; atual = atual.proximo) {
            if (atual.item.getCodigo().equals(codigo)) {
                return atual;
            }
        }
        return null;
    }

    private static void validar(ItemEstoque item) {
        if (item == null) {
            throw new IllegalArgumentException("Item nulo");
        }
        if (!Validacao.textoPreenchido(item.getCodigo(), ItemEstoque.TAM_CODIGO)) {
            throw new IllegalArgumentException("Codigo invalido");
        }
        if (!Validacao.tipoSanguineoValido(item.getTipoSanguineo())) {
            throw new IllegalArgumentException("Tipo sanguineo invalido");
        }
        if (item.getComponente() == null) {
            throw new IllegalArgumentException("Hemocomponente invalido");
        }
        if (item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (!Validacao.dataValida(item.getValidade())) {
            throw new IllegalArgumentException("Validade invalida (use AAAA-MM-DD)");
        }
    }
}
