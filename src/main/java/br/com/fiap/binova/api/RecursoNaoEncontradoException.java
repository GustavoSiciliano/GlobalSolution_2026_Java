
package br.com.fiap.binova.api;

public class RecursoNaoEncontradoException extends RuntimeException {

    // Informa que o recurso solicitado não foi encontrado
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
