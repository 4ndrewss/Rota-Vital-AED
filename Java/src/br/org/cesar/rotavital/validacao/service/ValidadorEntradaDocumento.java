package br.org.cesar.rotavital.validacao.service;

import br.org.cesar.rotavital.validacao.exception.DocumentoInvalidoException;
import br.org.cesar.rotavital.validacao.exception.DocumentoInvalidoException.DetalheErroCampo;
import br.org.cesar.rotavital.validacao.model.DadosRequisicao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Serviço responsável pela validação de entrada de requisições de hemocomponentes.
 * Realiza a validação física do arquivo recebido e a validação semântica dos dados extraídos.
 */
public class ValidadorEntradaDocumento {

    private static final Pattern PREFIXO_AGENCIA_REGEX = Pattern.compile("^[A-Z]{2,4}-[0-9]{3,5}$");
    private static final Pattern NOME_RESPONSAVEL_REGEX = Pattern.compile("^[A-Za-zÀ-ÿ\\s\\.\\-']{3,100}$");
    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Valida os bytes do arquivo recebido:
     * - Não pode ser vazio (0 bytes)
     * - Não pode ter menos que 1 KB (arquivo corrompido ou nulo)
     * - Não pode ultrapassar 10 MB
     * - Valida Magic Bytes (PDF, PNG, JPEG)
     */
    public void validarArquivo(byte[] bytesArquivo, String nomeArquivo) {
        List<DetalheErroCampo> erros = new ArrayList<>();

        if (bytesArquivo == null || bytesArquivo.length == 0) {
            throw new DocumentoInvalidoException("arquivo", "ARQUIVO_VAZIO",
                    "O arquivo submetido está vazio ou possui 0 bytes.", 0);
        }

        if (bytesArquivo.length < 1024) {
            erros.add(new DetalheErroCampo("arquivo", "ARQUIVO_CORROMPIDO",
                    "Arquivo corrompido ou incompleto: tamanho inferior a 1 KB.", bytesArquivo.length));
        }

        if (bytesArquivo.length > 10 * 1024 * 1024) {
            erros.add(new DetalheErroCampo("arquivo", "TAMANHO_EXCEDIDO",
                    "O tamanho do arquivo excede o limite máximo permitido de 10 MB.", bytesArquivo.length));
        }

        // Inspeção de Magic Bytes no cabeçalho
        if (bytesArquivo.length >= 4) {
            boolean isPdf = bytesArquivo[0] == 0x25 && bytesArquivo[1] == 0x50 
                         && bytesArquivo[2] == 0x44 && bytesArquivo[3] == 0x46; // %PDF
            boolean isPng = bytesArquivo.length >= 8 && (bytesArquivo[0] & 0xFF) == 0x89 
                         && bytesArquivo[1] == 0x50 && bytesArquivo[2] == 0x4E && bytesArquivo[3] == 0x47;
            boolean isJpeg = (bytesArquivo[0] & 0xFF) == 0xFF && (bytesArquivo[1] & 0xFF) == 0xD8 
                          && (bytesArquivo[2] & 0xFF) == 0xFF;

            if (!isPdf && !isPng && !isJpeg) {
                erros.add(new DetalheErroCampo("arquivo", "TIPO_ARQUIVO_NAO_SUPORTADO",
                        "Formato não suportado. O documento deve ser um PDF ou imagem JPEG/PNG válida.", nomeArquivo));
            }
        }

        if (!erros.isEmpty()) {
            throw new DocumentoInvalidoException("Arquivo rejeitado na análise física/binária.", erros);
        }
    }

    /**
     * Valida os metadados extraídos pelo OCR:
     * - Presença e legibilidade de data, prefixo, nome do responsável e assinatura.
     * - Formato de data (não futura, não expirada há mais de 30 dias).
     * - Prefixo da agência no padrão esperado [SIGLA]-[NUMERO].
     * - Nome do responsável alfabético com tamanho válido.
     * - Assinatura presente = true.
     */
    public void validarMetadados(DadosRequisicao dados) {
        List<DetalheErroCampo> erros = new ArrayList<>();

        if (dados == null) {
            throw new DocumentoInvalidoException("requisicao", "DADOS_NULOS",
                    "Os dados da requisição não foram informados.", null);
        }

        // 1. Data
        if (isVazioOuIlegivel(dados.getData())) {
            erros.add(new DetalheErroCampo("data", "CAMPO_OBRIGATORIO_AUSENTE",
                    "O campo 'data' é obrigatório e está ausente ou ilegível.", dados.getData()));
        } else {
            LocalDate dataConvertida = parseData(dados.getData());
            if (dataConvertida == null) {
                erros.add(new DetalheErroCampo("data", "DATA_FORMATO_INVALIDO",
                        "O campo 'data' possui formato inválido. Use 'YYYY-MM-DD' ou 'DD/MM/YYYY'.", dados.getData()));
            } else if (dataConvertida.isAfter(LocalDate.now())) {
                erros.add(new DetalheErroCampo("data", "DATA_FUTURA_INVALIDA",
                        "A data do documento não pode ser posterior à data atual.", dados.getData()));
            } else if (dataConvertida.isBefore(LocalDate.now().minusDays(30))) {
                erros.add(new DetalheErroCampo("data", "DATA_EXPIRADA",
                        "A requisição foi emitida há mais de 30 dias e está expirada para uso clínico.", dados.getData()));
            }
        }

        // 2. Prefixo da Agência
        if (isVazioOuIlegivel(dados.getPrefixoAgencia())) {
            erros.add(new DetalheErroCampo("prefixoAgencia", "CAMPO_OBRIGATORIO_AUSENTE",
                    "O campo 'prefixoAgencia' é obrigatório e está ausente ou ilegível.", dados.getPrefixoAgencia()));
        } else if (!PREFIXO_AGENCIA_REGEX.matcher(dados.getPrefixoAgencia().trim().toUpperCase()).matches()) {
            erros.add(new DetalheErroCampo("prefixoAgencia", "PREFIXO_AGENCIA_INVALIDO",
                    "O campo 'prefixoAgencia' deve seguir o formato regulatório '[SIGLA]-[NUMERO]' (Ex: 'AG-1002' ou 'HEMO-014').",
                    dados.getPrefixoAgencia()));
        }

        // 3. Nome do Responsável
        if (isVazioOuIlegivel(dados.getNomeResponsavel())) {
            erros.add(new DetalheErroCampo("nomeResponsavel", "CAMPO_OBRIGATORIO_AUSENTE",
                    "O campo 'nomeResponsavel' é obrigatório e está ausente ou ilegível.", dados.getNomeResponsavel()));
        } else if (!NOME_RESPONSAVEL_REGEX.matcher(dados.getNomeResponsavel().trim()).matches()) {
            erros.add(new DetalheErroCampo("nomeResponsavel", "NOME_RESPONSAVEL_INVALIDO",
                    "O campo 'nomeResponsavel' deve conter entre 3 e 100 caracteres alfabéticos válidos.", dados.getNomeResponsavel()));
        }

        // 4. Assinatura Presente
        if (dados.getAssinaturaPresente() == null || !dados.getAssinaturaPresente()) {
            erros.add(new DetalheErroCampo("assinaturaPresente", "ASSINATURA_AUSENTE",
                    "A assinatura física ou digital do responsável técnico não foi localizada no documento.", false));
        }

        if (!erros.isEmpty()) {
            throw new DocumentoInvalidoException("Falha na validação dos campos obrigatórios da requisição.", erros);
        }
    }

    private boolean isVazioOuIlegivel(String valor) {
        if (valor == null || valor.trim().isEmpty()) return true;
        String limpo = valor.trim().toUpperCase();
        return limpo.equals("[ILEGIVEL]") || limpo.equals("[UNREADABLE]") 
            || limpo.equals("[ERRO_LEITURA]") || limpo.equals("[DESCONHECIDO]");
    }

    private LocalDate parseData(String dataTexto) {
        try {
            return LocalDate.parse(dataTexto.trim(), FORMATO_ISO);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(dataTexto.trim(), FORMATO_BR);
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }
}
