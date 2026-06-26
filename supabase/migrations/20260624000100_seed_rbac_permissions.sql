INSERT INTO permiso (codigo_permiso, descripcion) VALUES
('USUARIO_LEER', 'Consultar usuarios'),
('USUARIO_CREAR', 'Crear usuarios'),
('USUARIO_MODIFICAR', 'Modificar usuarios'),
('USUARIO_ELIMINAR', 'Eliminar o desactivar usuarios'),
('ROL_LEER', 'Consultar roles y permisos'),
('ROL_CREAR', 'Crear roles'),
('ROL_MODIFICAR', 'Modificar roles y permisos'),
('ROL_ELIMINAR', 'Eliminar o desactivar roles'),
('CATALOGO_SEXO_LEER', 'Consultar catalogo de sexos'),
('CATALOGO_SEXO_CREAR', 'Crear valores del catalogo de sexos'),
('CATALOGO_SEXO_MODIFICAR', 'Modificar valores del catalogo de sexos'),
('CATALOGO_SEXO_ELIMINAR', 'Eliminar logicamente valores del catalogo de sexos'),
('CARRERA_LEER', 'Consultar carreras'),
('CARRERA_CREAR', 'Crear carreras'),
('CARRERA_MODIFICAR', 'Modificar carreras'),
('CARRERA_ELIMINAR', 'Eliminar logicamente carreras'),
('COHORTE_LEER', 'Consultar cohortes'),
('COHORTE_CREAR', 'Crear cohortes'),
('COHORTE_MODIFICAR', 'Modificar cohortes'),
('COHORTE_ELIMINAR', 'Eliminar logicamente cohortes'),
('GRUPO_ACADEMICO_LEER', 'Consultar grupos academicos'),
('GRUPO_ACADEMICO_CREAR', 'Crear grupos academicos'),
('GRUPO_ACADEMICO_MODIFICAR', 'Modificar grupos academicos'),
('GRUPO_ACADEMICO_ELIMINAR', 'Eliminar logicamente grupos academicos'),
('PARTICIPANTE_LEER', 'Consultar participantes'),
('PARTICIPANTE_CREAR', 'Crear participantes'),
('PARTICIPANTE_MODIFICAR', 'Modificar participantes'),
('PARTICIPANTE_ELIMINAR', 'Eliminar logicamente participantes'),
('PARTICIPANTE_ACCESO_GESTIONAR', 'Generar y administrar tokens de acceso de participantes'),
('CALIFICACION_EJECUTAR', 'Ejecutar calificacion oficial de intentos'),
('RESULTADO_AGREGADO_VER', 'Consultar resultados agregados'),
('AUDITORIA_REGISTRAR', 'Registrar eventos de auditoria desde servicios')
ON CONFLICT (codigo_permiso) DO UPDATE SET descripcion = EXCLUDED.descripcion;

DELETE FROM rol_permiso
WHERE permiso_id IN (
    SELECT permiso_id FROM permiso
    WHERE codigo_permiso IN ('USUARIO_GESTIONAR', 'ROL_PERMISO_GESTIONAR', 'PARTICIPANTE_GESTIONAR', 'RESPUESTA_REGISTRAR')
);

DELETE FROM permiso
WHERE codigo_permiso IN ('USUARIO_GESTIONAR', 'ROL_PERMISO_GESTIONAR', 'PARTICIPANTE_GESTIONAR', 'RESPUESTA_REGISTRAR');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.rol_id, p.permiso_id
FROM rol r
CROSS JOIN permiso p
WHERE r.nombre_rol = 'ADMINISTRADOR'
   OR (r.nombre_rol = 'PSICOLOGO_COORDINADOR' AND p.codigo_permiso IN (
        'CATALOGO_SEXO_LEER', 'CARRERA_LEER', 'COHORTE_LEER', 'GRUPO_ACADEMICO_LEER',
        'TEST_CREAR', 'TEST_PUBLICAR','TEST_LEER', 'CALIFICACION_CONFIGURAR', 'BAREMO_CONFIGURAR',
        'RUBRICA_CONFIGURAR', 'RESULTADO_VER', 'RESULTADO_AGREGADO_VER',
        'REPORTE_EXPORTAR', 'CALIFICACION_EJECUTAR'
   ))
   OR (r.nombre_rol = 'APLICADOR' AND p.codigo_permiso IN (
        'CATALOGO_SEXO_LEER', 'CARRERA_LEER', 'COHORTE_LEER', 'GRUPO_ACADEMICO_LEER',
        'PARTICIPANTE_LEER', 'PARTICIPANTE_CREAR', 'PARTICIPANTE_MODIFICAR', 'PARTICIPANTE_ELIMINAR',
        'PARTICIPANTE_ACCESO_GESTIONAR', 'SESION_CREAR', 'SESION_APLICAR'
   ))
   OR (r.nombre_rol = 'CONSULTOR_REPORTES' AND p.codigo_permiso IN (
        'CATALOGO_SEXO_LEER', 'CARRERA_LEER', 'COHORTE_LEER', 'GRUPO_ACADEMICO_LEER',
        'PARTICIPANTE_LEER', 'RESULTADO_VER', 'RESULTADO_AGREGADO_VER', 'REPORTE_EXPORTAR'
   ))
ON CONFLICT DO NOTHING;
