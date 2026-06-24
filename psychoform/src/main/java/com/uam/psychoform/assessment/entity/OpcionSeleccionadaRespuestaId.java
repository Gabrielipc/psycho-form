package com.uam.psychoform.assessment.entity;

import jakarta.persistence.*;
import java.io.*;
import java.util.*;

@Embeddable
public class OpcionSeleccionadaRespuestaId implements Serializable {
    @Column(name = "respuesta_id")
    private Long respuestaId;
    @Column(name = "opcion_id")
    private Long opcionId;

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OpcionSeleccionadaRespuestaId x))
            return false;
        return Objects.equals(respuestaId, x.respuestaId) && Objects.equals(opcionId, x.opcionId);
    }

    public int hashCode() {
        return Objects.hash(respuestaId, opcionId);
    }
}
