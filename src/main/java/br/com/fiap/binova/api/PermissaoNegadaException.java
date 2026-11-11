
package br.com.fiap.binova.api;

public class PermissaoNegadaException extends RuntimeException {

    // Informa que o usuário não possui permissão para a operação
    public PermissaoNegadaException(String mensagem) {
        super(mensagem);
    }
}
