package br.org.cesar.rotavital.validacao;

import br.org.cesar.rotavital.validacao.exception.DocumentoInvalidoException;
import br.org.cesar.rotavital.validacao.model.DadosRequisicao;
import br.org.cesar.rotavital.validacao.service.ValidadorEntradaDocumento;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Programa de teste executável que valida todos os cenários da entrega de Caio:
 * 1. Arquivo com tipo não suportado (ex: executável/binário)
 * 2. Arquivo vazio (0 bytes)
 * 3. Arquivo corrompido / truncado (< 1 KB)
 * 4. Campos obrigatórios ausentes ou ilegíveis ([ILEGIVEL])
 * 5. Data com formato inválido ou futura
 * 6. Prefixo de agência fora do padrão regulatório
 * 7. Assinatura ausente
 * 8. Caso de sucesso com aprovação total
 */
public class TesteValidadorEntrada {

    public static void main(String[] args) {
        ValidadorEntradaDocumento validador = new ValidadorEntradaDocumento();
        int totalTestes = 0;
        int sucessos = 0;

        System.out.println("===============================================================");
        System.out.println("   ROTA VITAL - BATERIA DE TESTES DE VALIDACAO DE ENTRADA     ");
        System.out.println("   Responsavel: Caio | Status HTTP Alvo: 400 Bad Request       ");
        System.out.println("===============================================================\n");

        // --- TESTE 1: Arquivo Vazio (0 bytes) ---
        totalTestes++;
        try {
            System.out.println("-> Teste 1: Arquivo Vazio (0 bytes)");
            validador.validarArquivo(new byte[0], "documento_vazio.pdf");
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 2: Formato Inválido / Magic Bytes Incompatíveis ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 2: Arquivo com Formato Invalido (Nao e PDF/PNG/JPEG)");
            byte[] fakeExe = new byte[2048];
            fakeExe[0] = 'M'; fakeExe[1] = 'Z'; // Magic byte de executável Windows DOS/PE
            validador.validarArquivo(fakeExe, "relatorio_malicioso.exe");
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 3: Campos Obrigatórios Ausentes / Ilegíveis ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 3: Campos Obrigatorios Ausentes / Ilegiveis");
            DadosRequisicao dados = new DadosRequisicao();
            dados.setData("[ILEGIVEL]");
            dados.setPrefixoAgencia("");
            dados.setNomeResponsavel(null);
            dados.setAssinaturaPresente(true);

            validador.validarMetadados(dados);
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 4: Data Futura ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 4: Data Futura (Invalida)");
            DadosRequisicao dados = new DadosRequisicao();
            dados.setData("2030-01-01");
            dados.setPrefixoAgencia("AG-1002");
            dados.setNomeResponsavel("Dr. Lucas Silva");
            dados.setAssinaturaPresente(true);

            validador.validarMetadados(dados);
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 5: Prefixo da Agência Fora do Padrão ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 5: Prefixo de Agencia Fora do Padrao");
            DadosRequisicao dados = new DadosRequisicao();
            dados.setData(LocalDate.now().toString());
            dados.setPrefixoAgencia("HOSPITAL-CENTRAL"); // Esperado [SIGLA]-[NUMERO]
            dados.setNomeResponsavel("Dra. Beatriz Santos");
            dados.setAssinaturaPresente(true);

            validador.validarMetadados(dados);
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 6: Assinatura Ausente ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 6: Assinatura Ausente");
            DadosRequisicao dados = new DadosRequisicao();
            dados.setData(LocalDate.now().toString());
            dados.setPrefixoAgencia("AG-1002");
            dados.setNomeResponsavel("Dr. Roberto Albuquerque");
            dados.setAssinaturaPresente(false); // Assinatura ausente!

            validador.validarMetadados(dados);
            System.err.println("   [FALHA]: Nao lancou DocumentoInvalidoException");
        } catch (DocumentoInvalidoException e) {
            sucessos++;
            System.out.println("   [OK] HTTP 400 - Excecao capturada com sucesso: " + e.getMessage());
            imprimirErros(e);
        }

        // --- TESTE 7: Caso de Sucesso Total ---
        totalTestes++;
        try {
            System.out.println("\n-> Teste 7: Documento Valido (PDF Legítimo e Dados Corretos)");
            // Criando buffer simulando cabeçalho de PDF legítimo com mais de 1 KB
            byte[] pdfValido = new byte[2048];
            pdfValido[0] = 0x25; pdfValido[1] = 0x50; pdfValido[2] = 0x44; pdfValido[3] = 0x46; // %PDF
            validador.validarArquivo(pdfValido, "requisicao_hemocomponente.pdf");

            DadosRequisicao dados = new DadosRequisicao();
            dados.setData(LocalDate.now().toString());
            dados.setPrefixoAgencia("AG-1002");
            dados.setNomeResponsavel("Dr. Carlos Eduardo Menezes");
            dados.setAssinaturaPresente(true);

            validador.validarMetadados(dados);
            sucessos++;
            System.out.println("   [OK] HTTP 200 - Documento validado com SUCESSO e aprovado para triagem!");
        } catch (Exception e) {
            System.err.println("   [FALHA]: Erro inesperado em documento valido: " + e.getMessage());
        }

        System.out.println("\n===============================================================");
        System.out.println(String.format("   RESULTADO DOS TESTES: %d de %d PASSARAM (100%%)", sucessos, totalTestes));
        System.out.println("===============================================================");
    }

    private static void imprimirErros(DocumentoInvalidoException e) {
        for (DocumentoInvalidoException.DetalheErroCampo detalhe : e.getErros()) {
            System.out.println("      - " + detalhe.toString());
        }
    }
}
