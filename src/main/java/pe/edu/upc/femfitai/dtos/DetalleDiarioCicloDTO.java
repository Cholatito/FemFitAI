package pe.edu.upc.femfitai.dtos;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "Datos de DetalleDiarioCiclo; las referencias mantienen los IDs existentes")
public record DetalleDiarioCicloDTO(Integer idDetalleDiarioCiclo, Integer idCiclo, LocalDate fecha,
        String faseRegistrada,
        @JsonDeserialize(using = EnergiaEnteraDeserializer.class) Integer nivelEnergia,
        String observaciones) {
    // Evita que JSON fraccionario se trunque antes de validar la escala 1..5.
    public static class EnergiaEnteraDeserializer extends ValueDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) {
            if (parser.currentToken() != JsonToken.VALUE_NUMBER_INT) {
                return context.reportInputMismatch(Integer.class, "NivelEnergia debe ser un entero");
            }
            return parser.getIntValue();
        }
    }
}
