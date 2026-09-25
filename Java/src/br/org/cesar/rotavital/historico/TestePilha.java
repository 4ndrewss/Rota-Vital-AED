package br.org.cesar.rotavital.historico;

import static br.org.cesar.rotavital.historico.TipoOperacao.ENTRADA_ESTOQUE;
import static br.org.cesar.rotavital.historico.TipoOperacao.REMOCAO_ESTOQUE;
import static br.org.cesar.rotavital.historico.TipoOperacao.REQUISICAO_ATENDIDA;
import static br.org.cesar.rotavital.historico.TipoOperacao.REQUISICAO_RECEBIDA;
import static br.org.cesar.rotavital.historico.TipoOperacao.RETIRADA_ESTOQUE;

/**
 * Casos de teste do histórico, equivalentes a teste_pilha.c, com a mesma saída [PASSOU]/[FALHOU].
 * Sem dependências (sem JUnit) para rodar só com javac/java.
 */
public class TestePilha {

    private static int casosExecutados = 0;
    private static int casosFalhos = 0;

    private interface Caso {
        void executar();
    }

    private static void verificar(boolean condicao, String descricao) {
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void rodar(Caso caso, String descricao) {
        casosExecutados++;
        try {
            caso.executar();
            System.out.println("  [PASSOU] " + descricao);
        } catch (AssertionError | RuntimeException e) {
            casosFalhos++;
            System.out.println("  [FALHOU] " + descricao + "\n           -> " + e);
        }
    }

    private static Operacao novaOperacao(TipoOperacao tipo, String referencia, int quantidade) {
        return new Operacao(tipo, referencia, quantidade, "2026-09-23", "");
    }

    private static PilhaHistorico pilhaComTres() {
        PilhaHistorico pilha = new PilhaHistorico();
        pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 8));
        pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "PL-0001", 20));
        pilha.empilhar(novaOperacao(REQUISICAO_ATENDIDA, "REQ-1", 2));
        return pilha;
    }

    private static boolean rejeita(PilhaHistorico pilha, Operacao operacao) {
        try {
            pilha.empilhar(operacao);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /* ---------- Criar ---------- */

    private static void criarPilhaVazia() {
        PilhaHistorico pilha = new PilhaHistorico();
        verificar(pilha.vazia(), "pilha nova vazia");
        verificar(pilha.tamanho() == 0, "tamanho 0");
        verificar(pilha.topo() == null, "sem topo");
    }

    /* ---------- Inserir (push) ---------- */

    private static void empilharEmPilhaVazia() {
        PilhaHistorico pilha = new PilhaHistorico();
        int seq = pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 8));

        verificar(seq == 1, "primeira sequencia e 1");
        verificar(pilha.topo() != null, "tem topo");
        verificar(pilha.tamanho() == 1, "tamanho 1");
    }

    private static void empilharNovoTopoApontaParaAntigo() {
        PilhaHistorico pilha = new PilhaHistorico();
        pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 8));
        int seq = pilha.empilhar(novaOperacao(REQUISICAO_RECEBIDA, "REQ-1", 2));

        verificar(seq == 2, "segunda sequencia e 2");
        verificar(pilha.topo().getTipo() == REQUISICAO_RECEBIDA, "novo topo");
        verificar(pilha.tamanho() == 2, "tamanho 2");
        pilha.desempilhar();
        verificar(pilha.topo().getReferencia().equals("HM-0001"), "abaixo do topo fica o antigo topo");
    }

    private static void empilharDadosInvalidosERejeitado() {
        PilhaHistorico pilha = new PilhaHistorico();
        Operacao dataRuim = novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 1);
        dataRuim.setData("23/09/2026");

        verificar(rejeita(pilha, novaOperacao(ENTRADA_ESTOQUE, "", 1)), "sem referencia");
        verificar(rejeita(pilha, novaOperacao(RETIRADA_ESTOQUE, "HM-0001", -2)), "quantidade negativa");
        verificar(rejeita(pilha, dataRuim), "data invalida");
        verificar(rejeita(pilha, novaOperacao(null, "HM-0001", 1)), "tipo invalido");
        verificar(rejeita(pilha, null), "operacao nula");
        verificar(pilha.vazia(), "nada foi empilhado");
        verificar(pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 1)) == 1,
                "rejeicoes nao consomem sequencia");
    }

    /* ---------- Remover (pop) ---------- */

    private static void desempilharEmOrdemLifo() {
        PilhaHistorico pilha = pilhaComTres();

        Operacao saida = pilha.desempilhar();
        verificar(saida.getSequencia() == 3, "sai a sequencia 3");
        verificar(saida.getTipo() == REQUISICAO_ATENDIDA, "tipo da sequencia 3");
        saida = pilha.desempilhar();
        verificar(saida.getSequencia() == 2, "sai a sequencia 2");
        verificar(saida.getReferencia().equals("PL-0001"), "referencia da sequencia 2");
        verificar(pilha.desempilhar().getSequencia() == 1, "sai a sequencia 1");
    }

    private static void desempilharUltimoEsvaziaPilha() {
        PilhaHistorico pilha = new PilhaHistorico();
        pilha.empilhar(novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 8));
        pilha.desempilhar();

        verificar(pilha.topo() == null, "sem topo");
        verificar(pilha.tamanho() == 0, "tamanho 0");
    }

    private static void desempilharPilhaVaziaLancaExcecao() {
        PilhaHistorico pilha = new PilhaHistorico();
        try {
            pilha.desempilhar();
            verificar(false, "deveria lancar PilhaVaziaException");
        } catch (PilhaVaziaException e) {
            verificar(e.getMessage().equals("Historico vazio"), "mensagem da excecao");
        }
    }

    private static void reutilizarPilhaAposEsvaziar() {
        PilhaHistorico pilha = new PilhaHistorico();
        Operacao a = novaOperacao(ENTRADA_ESTOQUE, "HM-0001", 8);
        pilha.empilhar(a);
        pilha.desempilhar();

        pilha.empilhar(a);
        verificar(pilha.topo().getSequencia() == 2, "sequencia nao e reaproveitada");
    }

    /* ---------- Consultar ---------- */

    private static void consultarTopoSemRemover() {
        PilhaHistorico pilha = pilhaComTres();
        Operacao topo = pilha.topo();

        verificar(topo != null, "tem topo");
        verificar(topo.getTipo() == REQUISICAO_ATENDIDA, "topo e a mais recente");
        verificar(pilha.tamanho() == 3, "topo nao remove");
        verificar(new PilhaHistorico().topo() == null, "pilha vazia sem topo");
    }

    private static void consultarQuantidadePorTipo() {
        PilhaHistorico pilha = pilhaComTres();

        verificar(pilha.contarPorTipo(ENTRADA_ESTOQUE) == 2, "2 entradas");
        verificar(pilha.contarPorTipo(REQUISICAO_ATENDIDA) == 1, "1 atendida");
        verificar(pilha.contarPorTipo(REMOCAO_ESTOQUE) == 0, "0 remocoes");
        pilha.desempilhar();
        verificar(pilha.contarPorTipo(REQUISICAO_ATENDIDA) == 0, "atendida saiu com o pop");
    }

    public static void main(String[] args) {
        System.out.println("\n=== Testes: Historico de operacoes (pilha) - Java ===");

        System.out.println("\n Criar");
        rodar(TestePilha::criarPilhaVazia, "Criar pilha vazia");

        System.out.println("\n Inserir (push)");
        rodar(TestePilha::empilharEmPilhaVazia, "Empilhar em pilha vazia");
        rodar(TestePilha::empilharNovoTopoApontaParaAntigo, "Empilhar: novo topo aponta para o antigo");
        rodar(TestePilha::empilharDadosInvalidosERejeitado, "Empilhar dados invalidos e rejeitado");

        System.out.println("\n Remover (pop)");
        rodar(TestePilha::desempilharEmOrdemLifo, "Desempilhar em ordem inversa (LIFO)");
        rodar(TestePilha::desempilharUltimoEsvaziaPilha, "Desempilhar o ultimo esvazia a pilha");
        rodar(TestePilha::desempilharPilhaVaziaLancaExcecao, "Desempilhar pilha vazia lanca excecao");
        rodar(TestePilha::reutilizarPilhaAposEsvaziar, "Reutilizar a pilha depois de esvaziar");

        System.out.println("\n Consultar");
        rodar(TestePilha::consultarTopoSemRemover, "Consultar o topo sem remover");
        rodar(TestePilha::consultarQuantidadePorTipo, "Consultar quantidade por tipo de operacao");

        System.out.printf("%nResultado: %d/%d casos passaram%n", casosExecutados - casosFalhos, casosExecutados);
        System.exit(casosFalhos == 0 ? 0 : 1);
    }
}
