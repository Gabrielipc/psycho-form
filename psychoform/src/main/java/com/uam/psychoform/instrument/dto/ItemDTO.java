package com.uam.psychoform.instrument.dto;

import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.instrument.model.TipoItem;
import com.uam.psychoform.instrument.model.TipoRespuesta;
import com.uam.psychoform.security.model.EstadoGeneral;
import java.math.BigDecimal;
import java.util.List;

public record ItemDTO(Long id, String codigoItem, TipoItem tipoItem, TipoRespuesta tipoRespuesta, String enunciado,
        String instruccion, Integer numeroOrden, BigDecimal puntajeBase, Integer tiempoLimiteSegundos,
        Boolean esObligatorio, Boolean esConfidencial, EstadoGeneral estado, List<ImageResourceDTO> imagenes) {
    public static ItemDTO from(Item item, List<ImageResourceDTO> imagenes) {
        return new ItemDTO(item.getId(), item.getCodigoItem(), item.getTipoItem(), item.getTipoRespuesta(),
                item.getEnunciado(), item.getInstruccion(), item.getNumeroOrden(), item.getPuntajeBase(),
                item.getTiempoLimiteSegundos(), item.getEsObligatorio(), item.getEsConfidencial(), item.getEstado(),
                List.copyOf(imagenes));
    }
}
