
package br.com.fiap.binova.api;

public class OperacaoInvalidaException extends RuntimeException {

    // Informa que a operação não atende às regras do sistema
    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
