package pe.edu.upc.femfitai.services.implementations;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class Validaciones {
    private Validaciones() {}
    public static org.springframework.data.domain.Pageable paginar(int pagina, int tamano) {
        exigir(pagina >= 0 && tamano >= 1 && tamano <= 100, "Pagina >= 0 y tamano entre 1 y 100 requeridos");
        return org.springframework.data.domain.PageRequest.of(pagina, tamano);
    }
    public static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
    public static void texto(String valor, int maximo, String campo, boolean requerido) {
        exigir(!requerido || (valor != null && !valor.isBlank()), campo + " es obligatorio");
        exigir(valor == null || valor.length() <= maximo, campo + " supera " + maximo + " caracteres");
    }
    public static void positivo(Integer valor, String campo) {
        exigir(valor != null && valor > 0, campo + " debe ser mayor a cero");
    }
    public static void decimal(BigDecimal valor, String campo, boolean requerido, boolean admiteCero) {
        exigir(!requerido || valor != null, campo + " es obligatorio");
        if (valor != null) {
            exigir(admiteCero ? valor.signum() >= 0 : valor.signum() > 0, campo + " fuera de rango");
            exigir(valor.compareTo(new BigDecimal("9999.99")) <= 0
                    && valor.stripTrailingZeros().scale() <= 2, campo + " admite hasta 9999.99 y dos decimales");
        }
    }
    public static ResponseStatusException noEncontrado(String recurso) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " no encontrado");
    }
    public static void conflicto(boolean condicion, String mensaje) {
        if (condicion) throw new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }
}
