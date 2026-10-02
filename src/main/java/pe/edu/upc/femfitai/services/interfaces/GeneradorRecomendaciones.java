package pe.edu.upc.femfitai.services.interfaces;

/**
 * Puerto para el generador real, todavia no definido en US17/US20.
 * Recibe solamente IDs cuya propiedad ya fue verificada por el servicio.
 * El adaptador debera cargar datos de esa cuenta, aplicar la regla aprobada de
 * suficiencia de datos y generar contenido y motivo. No debe persistir registros.
 * La politica de datos minimos, el proveedor y sus limites siguen pendientes.
 */
public interface GeneradorRecomendaciones {
    Resultado generar(Integer idUsuario, Integer idRutina);

    record Resultado(String tipo, String contenido, String motivo) {}
}
