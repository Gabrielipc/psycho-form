package com.uam.psychoform.audit.entity;

import com.uam.psychoform.security.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auditoria")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auditoria_id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String accion;
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String entidad;
    @Size(max = 100)
    @Column(name = "entidad_id", length = 100)
    private String entidadId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_anterior", columnDefinition = "jsonb")
    private String valorAnterior;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valor_nuevo", columnDefinition = "jsonb")
    private String valorNuevo;
    @Size(max = 60)
    @Column(name = "direccion_ip", length = 60)
    private String direccionIp;
    @Column(name = "agente_usuario", columnDefinition = "text")
    private String agenteUsuario;
    @NotNull
    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
