package br.com.fiap.binova.api;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record Erro(String instante, int status, String mensagem, String caminho, Map<String, String> campos) {}

    // Padroniza a resposta de erros da API
    private ResponseEntity<Erro> responder(HttpStatus status, String mensagem, HttpServletRequest request, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new Erro(OffsetDateTime.now().toString(), status.value(),
            mensagem, request.getRequestURI(), campos));
    }

    // Retorna 404 para registro inexistente
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Erro> naoEncontrado(RecursoNaoEncontradoException erro, HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, erro.getMessage(), request, Map.of());
    }

    // Retorna 409 para uma regra de negócio violada
    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<Erro> operacaoInvalida(OperacaoInvalidaException erro, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, erro.getMessage(), request, Map.of());
    }

    // Retorna 403 quando não há permissão
    @ExceptionHandler(PermissaoNegadaException.class)
    public ResponseEntity<Erro> permissaoNegada(PermissaoNegadaException erro, HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, erro.getMessage(), request, Map.of());
    }

    // Reúne os erros de validação em uma resposta 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> camposInvalidos(MethodArgumentNotValidException erro, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        erro.getBindingResult().getFieldErrors().forEach(campo ->
            campos.putIfAbsent(campo.getField(), campo.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Dados enviados invalidos", request, campos);
    }

    // Retorna 400 para parâmetro ou JSON inválido
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
        ConstraintViolationException.class, IllegalArgumentException.class})
    public ResponseEntity<Erro> formatoInvalido(Exception erro, HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "Formato ou parametro invalido", request, Map.of());
    }

    // Traduz conflitos de integridade para HTTP 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> conflitoBanco(DataIntegrityViolationException erro, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, "Registro duplicado ou vinculado a outros dados", request, Map.of());
    }

    // Retorna 401 quando o login falha
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Erro> credencialInvalida(BadCredentialsException erro, HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, "Email ou senha incorretos", request, Map.of());
    }
}
