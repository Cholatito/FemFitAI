package pe.edu.upc.femfitai.services.implementations;

import java.net.URI;
import pe.edu.upc.femfitai.entities.Ejercicios;
import static pe.edu.upc.femfitai.services.implementations.Validaciones.*;

final class ImagenEjercicio {
    private ImagenEjercicio() {}

    static void asignar(Ejercicios ejercicio, String url, String autor, String licencia, String licenciaUrl) {
        String imagen = urlSegura(url, "ImagenUrl");
        texto(autor, 255, "ImagenAutor", false);
        texto(licencia, 255, "ImagenLicencia", false);
        String enlaceLicencia = urlSegura(licenciaUrl, "ImagenLicenciaUrl");
        ejercicio.setImagenUrl(imagen);
        ejercicio.setImagenAutor(imagen == null ? null : limpiar(autor));
        ejercicio.setImagenLicencia(imagen == null ? null : limpiar(licencia));
        ejercicio.setImagenLicenciaUrl(imagen == null ? null : enlaceLicencia);
    }

    private static String limpiar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String urlSegura(String valor, String campo) {
        String url = limpiar(valor);
        if (url == null) return null;
        texto(url, 2048, campo, false);
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException ex) {
            exigir(false, campo + " debe ser una URL HTTPS válida");
            return null;
        }
        exigir("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null
                && uri.getUserInfo() == null && uri.getFragment() == null,
                campo + " debe ser una URL HTTPS pública sin credenciales ni fragmentos");
        return url;
    }
}
