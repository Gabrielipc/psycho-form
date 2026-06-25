package com.uam.psychoform.assessment.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.instrument.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;

@Getter
@Setter
@Entity
@Table(name = "opcion_seleccionada_respuesta")
public class OpcionSeleccionadaRespuesta {
    @EmbeddedId
    private OpcionSeleccionadaRespuestaId id;
    @MapsId("respuestaId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    private RespuestaItem respuesta;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
    @MapsId("opcionId")
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opcion_id", insertable = false, updatable = false)
    private OpcionItem opcion;
    @NotNull
    @Column(name = "seleccionada_en", nullable = false)
    private LocalDateTime seleccionadaEn;
}
