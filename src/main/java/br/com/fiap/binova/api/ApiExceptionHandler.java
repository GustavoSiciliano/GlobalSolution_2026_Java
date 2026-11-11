
package br.com.fiap.binova.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record Erro(String instante, int status, String mensagem,
                       String caminho, Map<String, String> campos) {}

    // Padroniza as respostas de erro da API
    private ResponseEntity<Erro> responder(HttpStatus status, String mensagem,
                                           HttpServletRequest request, Map<String, String> campos) {

        Erro erro = new Erro(OffsetDateTime.now().toString(), status.value(),
                mensagem, request.getRequestURI(), campos);

        return ResponseEntity.status(status).body(erro);
    }

    // Retorna 404 quando o recurso não existe
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException erro, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, erro.getMessage(), request, Map.of());
    }

    // Retorna 409 quando uma operação viola uma regra de negócio
    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<Erro> operacaoInvalida(OperacaoInvalidaException erro, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, erro.getMessage(), request, Map.of());
    }

    // Retorna 403 quando o usuário não possui permissão
    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<Erro> permissaoNegada(PermissaoNegadaException erro, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, erro.getMessage(), request, Map.of());
    }

    // Retorna os erros de validação dos campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> camposInvalidos(MethodArgumentNotValidException erro,
                                                HttpServletRequest request) {

        Map<String, String> campos = new LinkedHashMap<>();

        erro.getBindingResult().getFieldErrors().forEach(campo ->
                campos.putIfAbsent(campo.getField(), campo.getDefaultMessage()));

        return responder(HttpStatus.BAD_REQUEST, "Dados enviados invalidos", request, campos);
    }

    // Retorna 400 para JSON ou parâmetros inválidos
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<Erro> formatoInvalido(Exception erro, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Formato ou parametro invalido", request, Map.of());
    }

    // Retorna 409 para conflitos de integridade no banco
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> conflitoBanco(DataIntegrityViolationException erro,
                                              HttpServletRequest request) {

        return responder(HttpStatus.CONFLICT,
                "Registro duplicado ou vinculado a outros dados", request, Map.of());
    }

    // Retorna 401 quando as credenciais estão incorretas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Erro> credencialInvalida(BadCredentialsException erro,
                                                   HttpServletRequest request) {

        return responder(HttpStatus.UNAUTHORIZED, "Email ou senha incorretos", request, Map.of());
    }
}
