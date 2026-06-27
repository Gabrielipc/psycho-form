package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.OpcionItem;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.math.BigDecimal;
import java.util.List;

public record OptionDTO(Long id, String codigoOpcion, String textoOpcion, Integer numeroOrden,
        BigDecimal valorOrdinal, EstadoGeneral estado, List<ImageResourceDTO> imagenes) {
    public static OptionDTO from(OpcionItem option, List<ImageResourceDTO> imagenes) {
        return new OptionDTO(option.getId(), option.getCodigoOpcion(), option.getTextoOpcion(),
                option.getNumeroOrden(), option.getValorOrdinal(), option.getEstado(), List.copyOf(imagenes));
    }
}
