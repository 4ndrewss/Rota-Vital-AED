package br.org.cesar.rotavital.fila;

import br.org.cesar.rotavital.comum.Validacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Fila de requisições hospitalares — lista encadeada com referências de início e fim
 * (equivalente a fila.h / fila.c).
 *
 * Política FIFO: as requisições são atendidas na ordem de chegada.
 * Enfileirar ocorre no `fim` e desenfileirar no `inicio`, ambos em O(1).
 *
 * Não usa java.util.Queue: os nós são implementados à mão, como na versão em C.
 * Na integração com Spring Boot, a classe pode virar um @Service sem mudanças;
 */
public class FilaRequisicoes {

    private static class NoRequisicao {
        final Requisicao requisicao;
        NoRequisicao proximo;

        NoRequisicao(Requisicao requisicao) {
            this.requisicao = requisicao;
        }
    }

    private NoRequisicao inicio;   /* próxima a ser atendida */
    private NoRequisicao fim;      /* última que chegou */
    private int tamanho;
    private int proximoId = 1;

    /**
     * Insere uma cópia da requisição no fim (enqueue) e retorna o id atribuído.
     *
     * @throws IllegalArgumentException se algum campo for inválido
     */
    public int enfileirar(Requisicao requisicao) {
        validar(requisicao);

        NoRequisicao novo = new NoRequisicao(new Requisicao(requisicao));
        novo.requisicao.setId(proximoId++);

        if (fim == null) {
            /* Fila vazia: o novo nó é ao mesmo tempo início e fim. */
            inicio = novo;
        } else {
            fim.proximo = novo;
        }
        fim = novo;
        tamanho++;
        return novo.requisicao.getId();
    }

    /**
     * Remove e retorna a requisição do início (dequeue).
     *
     * @throws FilaVaziaException se não houver requisições pendentes
     */
    public Requisicao desenfileirar() {
        if (inicio == null) {
            throw new FilaVaziaException();
        }
        NoRequisicao removido = inicio;
        inicio = removido.proximo;
        if (inicio == null) {
            /* Removemos o último nó: `fim` não pode continuar apontando para ele. */
            fim = null;
        }
        tamanho--;
        return removido.requisicao;
    }

    /** Retorna (uma cópia de) a requisição do início sem removê-la, ou null se vazia. */
    public Requisicao frente() {
        return inicio == null ? null : new Requisicao(inicio.requisicao);
    }

    /** Retorna (uma cópia de) a requisição com o id informado, ou null. */
    public Requisicao buscar(int id) {
        for (NoRequisicao atual = inicio; atual != null; atual = atual.proximo) {
            if (atual.requisicao.getId() == id) {
                return new Requisicao(atual.requisicao);
            }
        }
        return null;
    }

    /** Posição (1 = próxima a ser atendida) da requisição com o id, ou 0 se não existir. */
    public int posicao(int id) {
        int posicao = 1;
        for (NoRequisicao atual = inicio; atual != null; atual = atual.proximo, posicao++) {
            if (atual.requisicao.getId() == id) {
                return posicao;
            }
        }
        return 0;
    }

    /** Quantas requisições pendentes existem para o hospital informado. */
    public int contarPorHospital(String hospital) {
        int total = 0;
        if (hospital == null) {
            return 0;
        }
        for (NoRequisicao atual = inicio; atual != null; atual = atual.proximo) {
            if (hospital.equals(atual.requisicao.getHospital())) {
                total++;
            }
        }
        return total;
    }

    /** Cópia das requisições em ordem de atendimento (útil para expor como JSON). */
    public List<Requisicao> listar() {
        List<Requisicao> lista = new ArrayList<>(tamanho);
        for (NoRequisicao atual = inicio; atual != null; atual = atual.proximo) {
            lista.add(new Requisicao(atual.requisicao));
        }
        return lista;
    }

    public int tamanho() {
        return tamanho;
    }

    public boolean vazia() {
        return inicio == null;
    }

    public void imprimir() {
        System.out.printf("Fila de requisicoes (%d pendente(s)):%n", tamanho);
        if (inicio == null) {
            System.out.println("  (vazia)");
            return;
        }
        System.out.printf("  %-3s %-4s %-24s %-4s %-16s %4s  %s%n",
                "POS", "ID", "HOSPITAL", "TIPO", "COMPONENTE", "QTD", "DATA");
        int posicao = 1;
        for (NoRequisicao atual = inicio; atual != null; atual = atual.proximo, posicao++) {
            Requisicao r = atual.requisicao;
            System.out.printf("  %-3d %-4d %-24s %-4s %-16s %4d  %s%n",
                    posicao, r.getId(), r.getHospital(), r.getTipoSanguineo(),
                    r.getComponente().getNome(), r.getQuantidade(), r.getData());
        }
    }

    private static void validar(Requisicao requisicao) {
        if (requisicao == null) {
            throw new IllegalArgumentException("Requisicao nula");
        }
        if (!Validacao.textoPreenchido(requisicao.getHospital(), Requisicao.TAM_HOSPITAL)) {
            throw new IllegalArgumentException("Hospital invalido");
        }
        if (!Validacao.textoPreenchido(requisicao.getResponsavel(), Requisicao.TAM_RESPONSAVEL)) {
            throw new IllegalArgumentException("Responsavel invalido");
        }
        if (!Validacao.tipoSanguineoValido(requisicao.getTipoSanguineo())) {
            throw new IllegalArgumentException("Tipo sanguineo invalido");
        }
        if (requisicao.getComponente() == null) {
            throw new IllegalArgumentException("Hemocomponente invalido");
        }
        if (requisicao.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        if (!Validacao.dataValida(requisicao.getData())) {
            throw new IllegalArgumentException("Data invalida (use AAAA-MM-DD)");
        }
    }
}
