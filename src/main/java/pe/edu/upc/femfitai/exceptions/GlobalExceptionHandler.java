package pe.edu.upc.femfitai.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upc.femfitai.dtos.ErrorResponse;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> manejarResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                ex.getStatusCode().value(), ex.getReason(), request.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // por si agregamos @Valid en los dtos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(), mensaje, request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }

    // cualquier error porsiaca
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(
            Exception ex, HttpServletRequest request) {

        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocurrió un error inesperado",
                request.getRequestURI());
        return ResponseEntity.internalServerError().body(body);
    }
}
