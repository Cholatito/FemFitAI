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
    @ExceptionHandler({
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> manejarPeticionInvalida(
            Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                400, "Peticion invalida: revise tipos, formato JSON y parametros requeridos",
                request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(
            org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                409, "La operacion entra en conflicto con registros existentes o relacionados",
                request.getRequestURI()));
    }

    @ExceptionHandler(org.springframework.web.ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> manejarErrorHttp(
            org.springframework.web.ErrorResponseException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(
                ex.getStatusCode().value(), ex.getBody().getDetail(), request.getRequestURI()));
    }

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
