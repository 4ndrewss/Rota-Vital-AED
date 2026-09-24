package br.org.cesar.rotavital.fila;

import br.org.cesar.rotavital.comum.TipoHemocomponente;

import static br.org.cesar.rotavital.comum.TipoHemocomponente.CRIOPRECIPITADO;
import static br.org.cesar.rotavital.comum.TipoHemocomponente.HEMACIAS;
import static br.org.cesar.rotavital.comum.TipoHemocomponente.PLAQUETAS;
import static br.org.cesar.rotavital.comum.TipoHemocomponente.PLASMA;

/**
 * Casos de teste da fila, equivalentes a teste_fila.c, com a mesma saída [PASSOU]/[FALHOU].
 * Sem dependências (sem JUnit) para rodar só com javac/java.
 */
public class TesteFila {

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

    private static Requisicao novaRequisicao(String hospital, String tipo,
                                             TipoHemocomponente componente, int quantidade) {
        return new Requisicao(hospital, "Dra. Plantonista", tipo, componente, quantidade, "2026-09-23");
    }

    private static FilaRequisicoes filaComTres() {
        FilaRequisicoes fila = new FilaRequisicoes();
        fila.enfileirar(novaRequisicao("IMIP", "O-", HEMACIAS, 2));
        fila.enfileirar(novaRequisicao("Hospital da Restauracao", "A+", PLASMA, 1));
        fila.enfileirar(novaRequisicao("IMIP", "B-", PLAQUETAS, 3));
        return fila;
    }

    private static boolean rejeita(FilaRequisicoes fila, Requisicao requisicao) {
        try {
            fila.enfileirar(requisicao);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /* ---------- Criar ---------- */

    private static void criarFilaVazia() {
        FilaRequisicoes fila = new FilaRequisicoes();
        verificar(fila.vazia(), "fila nova vazia");
        verificar(fila.tamanho() == 0, "tamanho 0");
        verificar(fila.frente() == null, "sem frente");
    }

    /* ---------- Inserir (enqueue) ---------- */

    private static void enfileirarEmFilaVazia() {
        FilaRequisicoes fila = new FilaRequisicoes();
        int id = fila.enfileirar(novaRequisicao("IMIP", "O-", HEMACIAS, 2));

        verificar(id == 1, "primeiro id e 1");
        verificar(fila.tamanho() == 1, "tamanho 1");
        verificar(fila.frente().getId() == 1, "unico item e a frente");
    }

    private static void enfileirarVaiParaOFim() {
        FilaRequisicoes fila = new FilaRequisicoes();
        fila.enfileirar(novaRequisicao("IMIP", "O-", HEMACIAS, 2));
        int id = fila.enfileirar(novaRequisicao("IMIP", "A+", PLASMA, 1));

        verificar(id == 2, "segundo id e 2");
        verificar(fila.frente().getId() == 1, "frente nao muda");
        verificar(fila.posicao(2) == 2, "novo item fica no fim");
    }

    private static void enfileirarGuardaCopia() {
        FilaRequisicoes fila = new FilaRequisicoes();
        Requisicao original = novaRequisicao("IMIP", "O-", HEMACIAS, 2);
        fila.enfileirar(original);
        original.setHospital("Alterado depois");

        verificar(fila.frente().getHospital().equals("IMIP"), "fila nao e afetada pelo objeto original");
        fila.frente().setQuantidade(99);
        verificar(fila.frente().getQuantidade() == 2, "frente() devolve copia");
    }

    private static void enfileirarDadosInvalidosERejeitado() {
        FilaRequisicoes fila = new FilaRequisicoes();
        Requisicao dataRuim = novaRequisicao("IMIP", "O+", HEMACIAS, 1);
        Requisicao semResponsavel = novaRequisicao("IMIP", "O+", HEMACIAS, 1);
        Requisicao semComponente = novaRequisicao("IMIP", "O+", null, 1);
        dataRuim.setData("2026-13-01");
        semResponsavel.setResponsavel("");

        verificar(rejeita(fila, novaRequisicao("", "O+", HEMACIAS, 1)), "sem hospital");
        verificar(rejeita(fila, novaRequisicao("IMIP", "X", HEMACIAS, 1)), "tipo invalido");
        verificar(rejeita(fila, novaRequisicao("IMIP", "O+", HEMACIAS, -1)), "quantidade negativa");
        verificar(rejeita(fila, dataRuim), "data invalida");
        verificar(rejeita(fila, semResponsavel), "sem responsavel");
        verificar(rejeita(fila, semComponente), "sem componente");
        verificar(rejeita(fila, null), "requisicao nula");
        verificar(fila.vazia(), "nada foi inserido");
        verificar(fila.enfileirar(novaRequisicao("IMIP", "O+", HEMACIAS, 1)) == 1,
                "rejeicoes nao consomem id");
    }

    /* ---------- Remover (dequeue) ---------- */

    private static void desenfileirarEmOrdemFifo() {
        FilaRequisicoes fila = filaComTres();

        Requisicao saida = fila.desenfileirar();
        verificar(saida.getId() == 1, "sai o id 1");
        verificar(saida.getHospital().equals("IMIP"), "dados preservados");
        verificar(fila.desenfileirar().getId() == 2, "sai o id 2");
        verificar(fila.desenfileirar().getId() == 3, "sai o id 3");
    }

    private static void desenfileirarUltimoEsvaziaFila() {
        FilaRequisicoes fila = new FilaRequisicoes();
        fila.enfileirar(novaRequisicao("IMIP", "O-", HEMACIAS, 2));
        fila.desenfileirar();

        verificar(fila.vazia(), "fila vazia");
        verificar(fila.tamanho() == 0, "tamanho 0");
        verificar(fila.frente() == null, "sem frente");
    }

    private static void desenfileirarFilaVaziaLancaExcecao() {
        FilaRequisicoes fila = new FilaRequisicoes();
        try {
            fila.desenfileirar();
            verificar(false, "deveria lancar FilaVaziaException");
        } catch (FilaVaziaException e) {
            verificar(e.getMessage().equals("Fila vazia"), "mensagem da excecao");
        }
    }

    private static void reutilizarFilaAposEsvaziar() {
        FilaRequisicoes fila = new FilaRequisicoes();
        Requisicao a = novaRequisicao("IMIP", "AB-", CRIOPRECIPITADO, 1);
        fila.enfileirar(a);
        fila.desenfileirar();

        verificar(fila.enfileirar(a) == 2, "ids nao sao reaproveitados");
        verificar(fila.tamanho() == 1, "tamanho 1");
        verificar(fila.frente().getId() == 2, "novo item e a frente");
    }

    /* ---------- Consultar ---------- */

    private static void consultarFrenteSemRemover() {
        FilaRequisicoes fila = filaComTres();
        Requisicao r = fila.frente();

        verificar(r != null && r.getId() == 1, "frente e o id 1");
        verificar(fila.tamanho() == 3, "frente nao remove");
    }

    private static void consultarPorId() {
        FilaRequisicoes fila = filaComTres();
        Requisicao r = fila.buscar(3);

        verificar(r != null, "encontra id 3");
        verificar(r.getTipoSanguineo().equals("B-"), "dados do id 3");
        verificar(fila.buscar(99) == null, "id inexistente");
    }

    private static void consultarPosicaoNaFila() {
        FilaRequisicoes fila = filaComTres();

        verificar(fila.posicao(1) == 1, "id 1 na posicao 1");
        verificar(fila.posicao(3) == 3, "id 3 na posicao 3");
        verificar(fila.posicao(99) == 0, "inexistente retorna 0");
        fila.desenfileirar();
        verificar(fila.posicao(3) == 2, "todos avancam uma posicao");
    }

    private static void consultarPendenciasPorHospital() {
        FilaRequisicoes fila = filaComTres();

        verificar(fila.contarPorHospital("IMIP") == 2, "IMIP tem 2");
        verificar(fila.contarPorHospital("Hospital Inexistente") == 0, "inexistente tem 0");
        fila.desenfileirar();
        verificar(fila.contarPorHospital("IMIP") == 1, "IMIP fica com 1");
    }

    private static void listarEmOrdemDeAtendimento() {
        FilaRequisicoes fila = filaComTres();
        java.util.List<Requisicao> lista = fila.listar();

        verificar(lista.size() == 3, "lista com 3");
        verificar(lista.get(0).getId() == 1 && lista.get(2).getId() == 3, "ordem FIFO");
    }

    public static void main(String[] args) {
        System.out.println("\n=== Testes: Requisicoes hospitalares (fila) - Java ===");

        System.out.println("\n Criar");
        rodar(TesteFila::criarFilaVazia, "Criar fila vazia");

        System.out.println("\n Inserir (enqueue)");
        rodar(TesteFila::enfileirarEmFilaVazia, "Enfileirar em fila vazia");
        rodar(TesteFila::enfileirarVaiParaOFim, "Enfileirar vai para o fim");
        rodar(TesteFila::enfileirarGuardaCopia, "Enfileirar guarda uma copia da requisicao");
        rodar(TesteFila::enfileirarDadosInvalidosERejeitado, "Enfileirar dados invalidos e rejeitado");

        System.out.println("\n Remover (dequeue)");
        rodar(TesteFila::desenfileirarEmOrdemFifo, "Desenfileirar em ordem de chegada (FIFO)");
        rodar(TesteFila::desenfileirarUltimoEsvaziaFila, "Desenfileirar o ultimo esvazia a fila");
        rodar(TesteFila::desenfileirarFilaVaziaLancaExcecao, "Desenfileirar fila vazia lanca excecao");
        rodar(TesteFila::reutilizarFilaAposEsvaziar, "Reutilizar a fila depois de esvaziar");

        System.out.println("\n Consultar");
        rodar(TesteFila::consultarFrenteSemRemover, "Consultar a frente sem remover");
        rodar(TesteFila::consultarPorId, "Consultar por id");
        rodar(TesteFila::consultarPosicaoNaFila, "Consultar posicao na fila");
        rodar(TesteFila::consultarPendenciasPorHospital, "Consultar pendencias por hospital");
        rodar(TesteFila::listarEmOrdemDeAtendimento, "Listar em ordem de atendimento");

        System.out.printf("%nResultado: %d/%d casos passaram%n", casosExecutados - casosFalhos, casosExecutados);
        System.exit(casosFalhos == 0 ? 0 : 1);
    }
}
