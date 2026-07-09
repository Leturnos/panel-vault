package io.github.leturnos.panelvault.exception;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    // Dynamically fallback to the application's configured Jackson time-zone to format timestamps consistently.
    @Value("${spring.jackson.time-zone:UTC}")
    private String timeZone;

    private ZoneId getZoneId() {
        return ZoneId.of(timeZone);
    }

    private String getFormattedTimestamp() {
        return timestampFormatter.withZone(getZoneId()).format(Instant.now());
    }

    // Fallback for unhandled server errors, ensuring internal stack traces do not leak to the client.
    @ExceptionHandler(Exception.class)
    protected final ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Internal server error: {}", ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro inesperado no servidor."
        );
        problemDetail.setTitle("Erro Interno do Servidor");
        problemDetail.setProperty("timestamp", getFormattedTimestamp());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @Nonnull HttpHeaders headers,
            @Nonnull HttpStatusCode status,
            @Nonnull WebRequest request) {

        // Extracts specific field errors and formats them as a list
        List<FieldErrorRecord> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorRecord(error.getField(), error.getDefaultMessage()))
                .toList();

        logger.warn("Validation error on fields: {}", fieldErrors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                "Os parâmetros da requisição não foram validados."
        );
        problemDetail.setTitle("Falha na Validação");
        problemDetail.setProperty("timestamp", getFormattedTimestamp());
        problemDetail.setProperty("errors", fieldErrors);

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected final ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logger.warn("Resource not found: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Recurso Não Encontrado");
        problemDetail.setProperty("timestamp", getFormattedTimestamp());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }

    // Catch database constraints (e.g. unique, foreign keys) and translate them into API conflict status (409).
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected final ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request) {
        logger.warn("Data integrity violation: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Erro de restrição de integridade de dados."
        );
        problemDetail.setTitle("Violação de Integridade de Dados");
        problemDetail.setProperty("timestamp", getFormattedTimestamp());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    protected final ResponseEntity<Object> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        logger.warn("Resource conflict: {}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Conflito de Recursos");
        problemDetail.setProperty("timestamp", getFormattedTimestamp());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
    }

    // Must re-throw AccessDeniedException so Spring Security's filter chain handles it (e.g. login redirect or custom entrypoint).
    @ExceptionHandler(AccessDeniedException.class)
    protected final ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        throw ex;
    }

    private record FieldErrorRecord(String field, String message) {}
}