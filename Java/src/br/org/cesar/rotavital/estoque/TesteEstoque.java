package br.org.cesar.rotavital.estoque;

import br.org.cesar.rotavital.comum.TipoHemocomponente;

import java.util.List;

import static br.org.cesar.rotavital.comum.TipoHemocomponente.HEMACIAS;
import static br.org.cesar.rotavital.comum.TipoHemocomponente.PLAQUETAS;
import static br.org.cesar.rotavital.comum.TipoHemocomponente.PLASMA;

/**
 * Casos de teste do estoque, equivalentes a teste_estoque.c, com a mesma saída [PASSOU]/[FALHOU].
 * Sem dependências (sem JUnit) para rodar só com javac/java.
 */
public class TesteEstoque {

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

    private static ItemEstoque novoItem(String codigo, String tipo, TipoHemocomponente componente,
                                        int quantidade, String validade) {
        return new ItemEstoque(codigo, tipo, componente, quantidade, validade);
    }

    private static boolean rejeita(Estoque estoque, ItemEstoque item) {
        try {
            estoque.inserir(item);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    /** Executa a ação e retorna o motivo da EstoqueException lançada, ou null se não lançou. */
    private static EstoqueException.Motivo motivoDoErro(Runnable acao) {
        try {
            acao.run();
            return null;
        } catch (EstoqueException e) {
            return e.getMotivo();
        }
    }

    /* ---------- Criar ---------- */

    private static void criarEstoqueVazio() {
        Estoque estoque = new Estoque();
        verificar(estoque.vazio(), "estoque novo vazio");
        verificar(estoque.tamanho() == 0, "tamanho 0");
        verificar(estoque.listar().isEmpty(), "lista vazia");
    }

    /* ---------- Inserir ---------- */

    private static void inserirEmListaVazia() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O-", HEMACIAS, 5, "2026-12-01"));

        verificar(estoque.tamanho() == 1, "tamanho 1");
        verificar(estoque.listar().get(0).getCodigo().equals("L001"), "L001 e o primeiro");
    }

    private static void inserirNoFimMantemOrdemDeCadastro() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O-", HEMACIAS, 5, "2026-12-01"));
        estoque.inserir(novoItem("L002", "A+", PLASMA, 3, "2026-10-15"));
        estoque.inserir(novoItem("L003", "B+", PLAQUETAS, 2, "2027-01-20"));
        estoque.inserir(novoItem("L004", "AB-", HEMACIAS, 1, "2026-11-10"));
        List<ItemEstoque> lista = estoque.listar();

        verificar(estoque.tamanho() == 4, "tamanho 4");
        verificar(lista.get(0).getCodigo().equals("L001"), "L001 primeiro");
        verificar(lista.get(1).getCodigo().equals("L002"), "L002 segundo");
        verificar(lista.get(2).getCodigo().equals("L003"), "L003 terceiro");
        verificar(lista.get(3).getCodigo().equals("L004"), "L004 ultimo");
    }

    private static void inserirGuardaCopia() {
        Estoque estoque = new Estoque();
        ItemEstoque original = novoItem("L001", "O+", HEMACIAS, 2, "2026-12-01");
        estoque.inserir(original);
        original.setQuantidade(99);

        verificar(estoque.buscar("L001").getQuantidade() == 2, "estoque nao e afetado pelo objeto original");
        estoque.buscar("L001").setQuantidade(50);
        verificar(estoque.buscar("L001").getQuantidade() == 2, "buscar() devolve copia");
    }

    private static void inserirCodigoDuplicadoERejeitado() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O+", HEMACIAS, 2, "2026-12-01"));

        verificar(motivoDoErro(() -> estoque.inserir(novoItem("L001", "A-", PLASMA, 1, "2026-12-05")))
                == EstoqueException.Motivo.CODIGO_DUPLICADO, "codigo duplicado");
        verificar(estoque.tamanho() == 1, "tamanho continua 1");
    }

    private static void inserirDadosInvalidosERejeitado() {
        Estoque estoque = new Estoque();

        verificar(rejeita(estoque, novoItem("L002", "C+", HEMACIAS, 1, "2026-12-01")), "tipo invalido");
        verificar(rejeita(estoque, novoItem("L003", "O+", HEMACIAS, 0, "2026-12-01")), "quantidade zero");
        verificar(rejeita(estoque, novoItem("L004", "O+", HEMACIAS, 1, "01/12/2026")), "data invalida");
        verificar(rejeita(estoque, novoItem("", "O+", HEMACIAS, 1, "2026-12-01")), "codigo vazio");
        verificar(rejeita(estoque, novoItem("L005", "O+", null, 1, "2026-12-01")), "sem componente");
        verificar(rejeita(estoque, null), "item nulo");
        verificar(estoque.tamanho() == 0, "nada foi inserido");
    }

    /* ---------- Remover ---------- */

    private static Estoque estoqueComTresItens() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O+", HEMACIAS, 1, "2026-10-01"));
        estoque.inserir(novoItem("L002", "O+", HEMACIAS, 1, "2026-11-01"));
        estoque.inserir(novoItem("L003", "O+", HEMACIAS, 1, "2026-12-01"));
        return estoque;
    }

    private static void removerDoInicio() {
        Estoque estoque = estoqueComTresItens();
        estoque.remover("L001");

        verificar(estoque.listar().get(0).getCodigo().equals("L002"), "L002 vira o inicio");
        verificar(estoque.tamanho() == 2, "tamanho 2");
    }

    private static void removerDoMeio() {
        Estoque estoque = estoqueComTresItens();
        estoque.remover("L002");
        List<ItemEstoque> lista = estoque.listar();

        verificar(lista.get(0).getCodigo().equals("L001") && lista.get(1).getCodigo().equals("L003"),
                "religou os vizinhos");
        verificar(estoque.tamanho() == 2, "tamanho 2");
    }

    private static void removerDoFim() {
        Estoque estoque = estoqueComTresItens();
        estoque.remover("L003");
        List<ItemEstoque> lista = estoque.listar();

        verificar(lista.size() == 2 && lista.get(1).getCodigo().equals("L002"), "L002 vira o ultimo");
        verificar(estoque.tamanho() == 2, "tamanho 2");
    }

    private static void removerUltimoItemEsvaziaLista() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O+", HEMACIAS, 1, "2026-10-01"));
        estoque.remover("L001");

        verificar(estoque.vazio(), "estoque vazio");
        verificar(estoque.tamanho() == 0, "tamanho 0");
    }

    private static void removerInexistenteRetornaErro() {
        Estoque estoque = estoqueComTresItens();
        Estoque vazio = new Estoque();

        verificar(motivoDoErro(() -> estoque.remover("L999")) == EstoqueException.Motivo.NAO_ENCONTRADO,
                "codigo inexistente");
        verificar(estoque.tamanho() == 3, "tamanho continua 3");
        verificar(motivoDoErro(() -> vazio.remover("L001")) == EstoqueException.Motivo.NAO_ENCONTRADO,
                "estoque vazio");
    }

    private static void retirarParcialETotal() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O+", HEMACIAS, 5, "2026-12-01"));

        try {
            estoque.retirar("L001", 0);
            verificar(false, "quantidade zero deveria ser rejeitada");
        } catch (IllegalArgumentException e) {
            /* esperado */
        }
        verificar(motivoDoErro(() -> estoque.retirar("L001", 6))
                == EstoqueException.Motivo.QUANTIDADE_INSUFICIENTE, "quantidade insuficiente");
        verificar(motivoDoErro(() -> estoque.retirar("L999", 1))
                == EstoqueException.Motivo.NAO_ENCONTRADO, "codigo inexistente");

        estoque.retirar("L001", 2);
        verificar(estoque.buscar("L001").getQuantidade() == 3, "sobram 3");
        estoque.retirar("L001", 3); /* zera e remove o nó */
        verificar(estoque.buscar("L001") == null, "item removido ao zerar");
        verificar(estoque.tamanho() == 0, "tamanho 0");
    }

    /* ---------- Consultar ---------- */

    private static Estoque estoqueParaConsulta() {
        Estoque estoque = new Estoque();
        estoque.inserir(novoItem("L001", "O-", HEMACIAS, 4, "2026-12-01"));
        estoque.inserir(novoItem("L002", "O-", HEMACIAS, 6, "2026-10-20"));
        estoque.inserir(novoItem("L003", "O-", PLASMA, 9, "2026-10-01"));
        estoque.inserir(novoItem("L004", "A+", HEMACIAS, 2, "2026-09-30"));
        return estoque;
    }

    private static void consultarPorCodigo() {
        Estoque estoque = estoqueParaConsulta();
        ItemEstoque item = estoque.buscar("L003");

        verificar(item != null, "encontra L003");
        verificar(item.getQuantidade() == 9, "quantidade 9");
        verificar(item.getComponente() == PLASMA, "componente plasma");
        verificar(estoque.buscar("L999") == null, "codigo inexistente");
    }

    private static void consultarTotalPorTipoEComponente() {
        Estoque estoque = estoqueParaConsulta();

        verificar(estoque.totalDisponivel("O-", HEMACIAS) == 10, "O- hemacias = 10");
        verificar(estoque.totalDisponivel("O-", PLASMA) == 9, "O- plasma = 9");
        verificar(estoque.totalDisponivel("B+", HEMACIAS) == 0, "B+ hemacias = 0");
    }

    public static void main(String[] args) {
        System.out.println("\n=== Testes: Estoque (lista encadeada) - Java ===");

        System.out.println("\n Criar");
        rodar(TesteEstoque::criarEstoqueVazio, "Criar estoque vazio");

        System.out.println("\n Inserir");
        rodar(TesteEstoque::inserirEmListaVazia, "Inserir em lista vazia");
        rodar(TesteEstoque::inserirNoFimMantemOrdemDeCadastro, "Inserir no fim mantem ordem de cadastro");
        rodar(TesteEstoque::inserirGuardaCopia, "Inserir guarda uma copia do item");
        rodar(TesteEstoque::inserirCodigoDuplicadoERejeitado, "Inserir codigo duplicado e rejeitado");
        rodar(TesteEstoque::inserirDadosInvalidosERejeitado, "Inserir dados invalidos e rejeitado");

        System.out.println("\n Remover");
        rodar(TesteEstoque::removerDoInicio, "Remover do inicio");
        rodar(TesteEstoque::removerDoMeio, "Remover do meio");
        rodar(TesteEstoque::removerDoFim, "Remover do fim");
        rodar(TesteEstoque::removerUltimoItemEsvaziaLista, "Remover o unico item esvazia a lista");
        rodar(TesteEstoque::removerInexistenteRetornaErro, "Remover codigo inexistente retorna erro");
        rodar(TesteEstoque::retirarParcialETotal, "Retirar bolsas (parcial e total)");

        System.out.println("\n Consultar");
        rodar(TesteEstoque::consultarPorCodigo, "Consultar por codigo");
        rodar(TesteEstoque::consultarTotalPorTipoEComponente, "Consultar total por tipo sanguineo e componente");

        System.out.printf("%nResultado: %d/%d casos passaram%n", casosExecutados - casosFalhos, casosExecutados);
        System.exit(casosFalhos == 0 ? 0 : 1);
    }
}
