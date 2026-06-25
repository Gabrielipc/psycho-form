INSERT INTO permiso (codigo_permiso, descripcion) VALUES ('PARTICIPANTE_GESTIONAR','Gestionar participantes') ON CONFLICT (codigo_permiso) DO NOTHING;
INSERT INTO rol_permiso (rol_id,permiso_id)
SELECT r.rol_id,p.permiso_id FROM rol r CROSS JOIN permiso p
WHERE (r.nombre_rol='ADMINISTRADOR')
   OR (r.nombre_rol='PSICOLOGO_COORDINADOR' AND p.codigo_permiso IN ('TEST_CREAR','TEST_PUBLICAR','CALIFICACION_CONFIGURAR','BAREMO_CONFIGURAR','RUBRICA_CONFIGURAR','RESULTADO_VER','REPORTE_EXPORTAR'))
   OR (r.nombre_rol='APLICADOR' AND p.codigo_permiso IN ('PARTICIPANTE_GESTIONAR','SESION_CREAR','SESION_APLICAR'))
   OR (r.nombre_rol='CONSULTOR_REPORTES' AND p.codigo_permiso IN ('RESULTADO_VER','REPORTE_EXPORTAR'))
ON CONFLICT DO NOTHING;
