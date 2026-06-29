WITH demo_users(nombre_usuario, correo, nombre_completo, rol_nombre) AS (
    VALUES
        ('admin', 'admin@local.test', 'Administrador Demo', 'ADMINISTRADOR'),
        ('aplicador', 'aplicador@local.test', 'Aplicador Demo', 'APLICADOR'),
        ('psicologo', 'psicologo@local.test', 'Psicologo Demo', 'PSICOLOGO_COORDINADOR'),
        ('consultor', 'consultor@local.test', 'Consultor Demo', 'CONSULTOR_REPORTES')
),
upserted_users AS (
    INSERT INTO usuario (
        nombre_usuario,
        correo,
        hash_contrasena,
        nombre_completo,
        estado,
        creado_en,
        actualizado_en
    )
    SELECT
        nombre_usuario,
        correo,
        '$2y$10$fiFS2OhO3xU3Ozq3Tlubl.m.k9PL6.9vlm3pnc/NuBZgAJiNh.zRe',
        nombre_completo,
        'ACTIVO',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM demo_users
    ON CONFLICT (nombre_usuario) DO UPDATE SET
        correo = EXCLUDED.correo,
        hash_contrasena = EXCLUDED.hash_contrasena,
        nombre_completo = EXCLUDED.nombre_completo,
        estado = EXCLUDED.estado,
        actualizado_en = CURRENT_TIMESTAMP
    RETURNING usuario_id, nombre_usuario
)
INSERT INTO usuario_rol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM upserted_users u
JOIN demo_users d ON d.nombre_usuario = u.nombre_usuario
JOIN rol r ON r.nombre_rol = d.rol_nombre
ON CONFLICT DO NOTHING;
