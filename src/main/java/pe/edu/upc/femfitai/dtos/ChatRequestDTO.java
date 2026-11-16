package pe.edu.upc.femfitai.dtos;

import java.util.List;

public record ChatRequestDTO(
        String mensaje,
        List<MensajeChatDTO> historial
) {}