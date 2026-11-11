package br.com.fiap.binova.api;
public class PermissaoNegadaException extends RuntimeException {
    public PermissaoNegadaException(String mensagem) {
        super(mensagem);
    }
}
