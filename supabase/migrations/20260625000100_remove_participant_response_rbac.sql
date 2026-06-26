DELETE FROM rol_permiso
WHERE permiso_id IN (
    SELECT permiso_id
    FROM permiso
    WHERE codigo_permiso = 'RESPUESTA_REGISTRAR'
);

DELETE FROM permiso
WHERE codigo_permiso = 'RESPUESTA_REGISTRAR';
