package pe.edu.upc.femfitai.services.interfaces;

public interface IWgerSyncService {

    ResultadoSincronizacion sincronizarTodos();

    record ResultadoSincronizacion(
            int totalConsultados,
            int insertados,
            int actualizados,
            int omitidos,
            boolean completo,
            Integer offsetError,
            String mensaje
    ) {
        public ResultadoSincronizacion(int totalConsultados, int insertados, int omitidos) {
            this(totalConsultados, insertados, 0, omitidos, true, null, null);
        }

        public ResultadoSincronizacion(int totalConsultados, int insertados, int actualizados, int omitidos) {
            this(totalConsultados, insertados, actualizados, omitidos, true, null, null);
        }
    }
}