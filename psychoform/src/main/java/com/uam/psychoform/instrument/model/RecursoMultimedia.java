package com.uam.psychoform.instrument.model;

import lombok.Getter;
import lombok.Setter;

import com.uam.psychoform.security.model.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "recurso_multimedia")
public class RecursoMultimedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurso_id")
    private Long id;
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tipo_recurso", nullable = false, columnDefinition = "tipo_recurso")
    private TipoRecurso tipoRecurso;
    @NotBlank
    @Size(max = 255)
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;
    @NotBlank
    @Column(name = "ruta_almacenamiento", nullable = false, columnDefinition = "text")
    private String rutaAlmacenamiento;
    @NotBlank
    @Size(max = 100)
    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;
    @PositiveOrZero
    private Long tamanoBytes;
    @Size(max = 128)
    @Column(name = "hash_integridad", length = 128)
    private String hashIntegridad;
    @NotNull
    private Boolean esConfidencial;
    @NotNull
    private Boolean requiereAutorizacion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por")
    private Usuario subidoPor;
    @NotNull
    @Column(name = "subido_en", nullable = false)
    private LocalDateTime subidoEn;
}
