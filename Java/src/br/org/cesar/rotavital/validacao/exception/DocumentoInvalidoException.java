package br.org.cesar.rotavital.validacao.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exceção customizada lançada quando um documento submetido ao Rota Vital
 * não atende aos critérios de validação de entrada (física ou semântica).
 * 
 * Contém informações detalhadas de cada campo que falhou.
 */
public class DocumentoInvalidoException extends RuntimeException {

    private final List<DetalheErroCampo> erros;

    public DocumentoInvalidoException(String mensagem) {
        super(mensagem);
        this.erros = new ArrayList<>();
    }

    public DocumentoInvalidoException(String campo, String codigo, String motivo, Object valorRecebido) {
        super(String.format("Falha na validação do campo '%s': %s", campo, motivo));
        this.erros = new ArrayList<>();
        this.erros.add(new DetalheErroCampo(campo, codigo, motivo, valorRecebido));
    }

    public DocumentoInvalidoException(String mensagemGeral, List<DetalheErroCampo> erros) {
        super(mensagemGeral);
        this.erros = erros != null ? new ArrayList<>(erros) : new ArrayList<>();
    }

    public List<DetalheErroCampo> getErros() {
        return Collections.unmodifiableList(erros);
    }

    public static class DetalheErroCampo {
        private final String campo;
        private final String codigo;
        private final String mensagem;
        private final Object valorRecebido;

        public DetalheErroCampo(String campo, String codigo, String mensagem, Object valorRecebido) {
            this.campo = campo;
            this.codigo = codigo;
            this.mensagem = mensagem;
            this.valorRecebido = valorRecebido;
        }

        public String getCampo() {
            return campo;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getMensagem() {
            return mensagem;
        }

        public Object getValorRecebido() {
            return valorRecebido;
        }

        @Override
        public String toString() {
            return String.format("Erro Campo: '%s' | Código: %s | Motivo: %s | Valor Recebido: %s",
                    campo, codigo, mensagem, valorRecebido);
        }
    }
}
