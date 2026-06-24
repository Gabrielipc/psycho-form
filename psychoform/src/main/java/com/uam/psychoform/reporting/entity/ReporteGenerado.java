package com.uam.psychoform.reporting.entity;

import com.uam.psychoform.assessment.entity.*;
import com.uam.psychoform.scoring.entity.*;
import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "reporte_generado")
public class ReporteGenerado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reporte_id")
    private Long id;
    @NotBlank
    @Size(max = 50)
    @Column(name = "tipo_reporte", nullable = false, length = 50)
    private String tipoReporte;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intento_id")
    private IntentoTest intento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_id")
    private Resultado resultado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sesion_aplicacion_id")
    private SesionAplicacion sesionAplicacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generado_por")
    private Usuario generadoPor;
    @NotNull
    @Column(name = "generado_en", nullable = false)
    private LocalDateTime generadoEn;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "formato_reporte")
    private FormatoReporte formato;
    @NotBlank
    @Column(name = "ruta_almacenamiento", nullable = false, columnDefinition = "text")
    private String rutaAlmacenamiento;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resumen_filtros", columnDefinition = "jsonb")
    private String resumenFiltros;
}
