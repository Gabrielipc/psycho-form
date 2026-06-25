INSERT INTO permiso (codigo_permiso, descripcion) VALUES
('PARTICIPANTE_GESTIONAR','Gestionar participantes'),
('PARTICIPANTE_ACCESO_GESTIONAR','Generar y administrar tokens de acceso de participantes'),
('RESPUESTA_REGISTRAR','Registrar respuestas de participantes durante la evaluacion'),
('CALIFICACION_EJECUTAR','Ejecutar calificacion oficial de intentos'),
('RESULTADO_AGREGADO_VER','Consultar resultados agregados'),
('AUDITORIA_REGISTRAR','Registrar eventos de auditoria desde servicios')
ON CONFLICT (codigo_permiso) DO NOTHING;
INSERT INTO rol_permiso (rol_id,permiso_id)
SELECT r.rol_id,p.permiso_id FROM rol r CROSS JOIN permiso p
WHERE (r.nombre_rol='ADMINISTRADOR')
   OR (r.nombre_rol='PSICOLOGO_COORDINADOR' AND p.codigo_permiso IN ('TEST_CREAR','TEST_PUBLICAR','CALIFICACION_CONFIGURAR','BAREMO_CONFIGURAR','RUBRICA_CONFIGURAR','RESULTADO_VER','RESULTADO_AGREGADO_VER','REPORTE_EXPORTAR','CALIFICACION_EJECUTAR'))
   OR (r.nombre_rol='APLICADOR' AND p.codigo_permiso IN ('PARTICIPANTE_GESTIONAR','PARTICIPANTE_ACCESO_GESTIONAR','SESION_CREAR','SESION_APLICAR','RESPUESTA_REGISTRAR'))
   OR (r.nombre_rol='CONSULTOR_REPORTES' AND p.codigo_permiso IN ('RESULTADO_VER','RESULTADO_AGREGADO_VER','REPORTE_EXPORTAR'))
ON CONFLICT DO NOTHING;
