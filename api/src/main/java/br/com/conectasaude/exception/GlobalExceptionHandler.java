package br.com.conectasaude.exception;

import br.com.conectasaude.dto.error.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException exception,
            HttpServletRequest request
    ) {

        return criarResposta(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> erros = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            erros.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                request,
                erros
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tratarParametroInvalido(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro: "
                        + exception.getName(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> tratarArgumentoInvalido(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {

        return criarResposta(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarErroInterno(
            Exception exception,
            HttpServletRequest request
    ) {

        logger.error(
                "Erro interno ao processar {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return criarResposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor",
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiError> criarResposta(
            HttpStatus status,
            String mensagem,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {

        ApiError erro = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(erro);
    }
}
