-- Datos funcionales para configurar y probar el instrumento BFA desde el backend.
-- La version 2.1 queda publicada para aplicaciones y la 2.2 en borrador para edicion.

INSERT INTO test (codigo_test, nombre_test, descripcion, creado_por, estado, creado_en)
VALUES (
    'BFA',
    'Bateria de Funciones Atencionales',
    'Instrumento inicial con subtests Figuras identicas, Desplazamiento y Espacial.',
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    'ACTIVO',
    TIMESTAMP '2026-06-01 08:00:00'
)
ON CONFLICT (codigo_test) DO UPDATE SET
    nombre_test = EXCLUDED.nombre_test,
    descripcion = EXCLUDED.descripcion,
    estado = EXCLUDED.estado;

INSERT INTO version_test (
    test_id,
    estrategia_calificacion_id,
    numero_version,
    estado,
    instrucciones_generales,
    tiempo_limite_segundos,
    permite_aleatorizar_subtests,
    permite_aleatorizar_items,
    aprobado_por,
    aprobado_en,
    publicado_en,
    creado_por,
    creado_en
)
VALUES
(
    (SELECT test_id FROM test WHERE codigo_test = 'BFA'),
    (SELECT estrategia_calificacion_id FROM estrategia_calificacion WHERE codigo = 'CLAVE_SIMPLE'),
    '2.0',
    'RETIRADO',
    'Version historica conservada como referencia administrativa.',
    1800,
    FALSE,
    FALSE,
    NULL,
    NULL,
    NULL,
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    TIMESTAMP '2025-11-20 08:00:00'
),
(
    (SELECT test_id FROM test WHERE codigo_test = 'BFA'),
    (SELECT estrategia_calificacion_id FROM estrategia_calificacion WHERE codigo = 'CLAVE_SIMPLE'),
    '2.1',
    'PUBLICADO',
    'Lea cada instruccion antes de iniciar. Responda cada item seleccionando una opcion.',
    1800,
    FALSE,
    FALSE,
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    TIMESTAMP '2026-05-10 09:00:00',
    TIMESTAMP '2026-05-10 10:00:00',
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    TIMESTAMP '2026-05-10 08:00:00'
),
(
    (SELECT test_id FROM test WHERE codigo_test = 'BFA'),
    (SELECT estrategia_calificacion_id FROM estrategia_calificacion WHERE codigo = 'CLAVE_SIMPLE'),
    '2.2',
    'BORRADOR',
    'Borrador editable para configurar subtests, items, opciones, imagenes y claves.',
    1800,
    FALSE,
    FALSE,
    NULL,
    NULL,
    NULL,
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    TIMESTAMP '2026-06-01 08:00:00'
)
ON CONFLICT (test_id, numero_version) DO UPDATE SET
    estrategia_calificacion_id = EXCLUDED.estrategia_calificacion_id,
    estado = EXCLUDED.estado,
    instrucciones_generales = EXCLUDED.instrucciones_generales,
    tiempo_limite_segundos = EXCLUDED.tiempo_limite_segundos,
    permite_aleatorizar_subtests = EXCLUDED.permite_aleatorizar_subtests,
    permite_aleatorizar_items = EXCLUDED.permite_aleatorizar_items,
    aprobado_por = EXCLUDED.aprobado_por,
    aprobado_en = EXCLUDED.aprobado_en,
    publicado_en = EXCLUDED.publicado_en;

INSERT INTO subtest (
    version_test_id,
    estrategia_calificacion_id,
    codigo_subtest,
    nombre_subtest,
    descripcion,
    instrucciones,
    numero_orden,
    tiempo_limite_segundos,
    permite_aleatorizar_items,
    permite_aleatorizar_opciones,
    es_obligatorio,
    estado
)
SELECT
    version.version_test_id,
    (SELECT estrategia_calificacion_id FROM estrategia_calificacion WHERE codigo = 'CLAVE_SIMPLE'),
    seed.codigo_subtest,
    seed.nombre_subtest,
    seed.descripcion,
    seed.instrucciones,
    seed.numero_orden,
    seed.tiempo_limite_segundos,
    FALSE,
    FALSE,
    TRUE,
    'ACTIVO'
FROM version_test version
JOIN test test ON test.test_id = version.test_id
CROSS JOIN (
    VALUES
        ('FIG_IDENTICAS', 'Figuras identicas', 'Comparacion visual de figuras.', 'Seleccione la figura identica al modelo.', 1, 480),
        ('DESPLAZAMIENTO', 'Desplazamiento', 'Identificacion de desplazamientos visuales.', 'Seleccione la opcion que representa el desplazamiento correcto.', 2, 600),
        ('ESPACIAL', 'Espacial', 'Razonamiento espacial con rotaciones y posiciones.', 'Seleccione la figura resultante de la transformacion espacial.', 3, 600)
) AS seed(codigo_subtest, nombre_subtest, descripcion, instrucciones, numero_orden, tiempo_limite_segundos)
WHERE test.codigo_test = 'BFA'
  AND version.numero_version IN ('2.1', '2.2')
ON CONFLICT (version_test_id, codigo_subtest) DO UPDATE SET
    nombre_subtest = EXCLUDED.nombre_subtest,
    descripcion = EXCLUDED.descripcion,
    instrucciones = EXCLUDED.instrucciones,
    numero_orden = EXCLUDED.numero_orden,
    tiempo_limite_segundos = EXCLUDED.tiempo_limite_segundos,
    estado = EXCLUDED.estado;

INSERT INTO item (
    subtest_id,
    codigo_item,
    tipo_item,
    tipo_respuesta,
    enunciado,
    instruccion,
    numero_orden,
    puntaje_base,
    tiempo_limite_segundos,
    es_obligatorio,
    es_confidencial,
    estado
)
SELECT
    subtest.subtest_id,
    subtest.codigo_subtest || '-' || LPAD(item_number::text, 3, '0'),
    'TEXTO_E_IMAGEN',
    'OPCION_UNICA',
    CASE subtest.codigo_subtest
        WHEN 'FIG_IDENTICAS' THEN 'Seleccione la figura identica al modelo'
        WHEN 'DESPLAZAMIENTO' THEN 'Seleccione la opcion que representa el desplazamiento correcto'
        ELSE 'Seleccione la figura resultante de la transformacion espacial'
    END,
    subtest.instrucciones,
    item_number,
    1.00,
    NULL,
    TRUE,
    TRUE,
    'ACTIVO'
FROM subtest
JOIN version_test version ON version.version_test_id = subtest.version_test_id
JOIN test test ON test.test_id = version.test_id
CROSS JOIN generate_series(1, 30) AS item_number
WHERE test.codigo_test = 'BFA'
  AND version.numero_version IN ('2.1', '2.2')
ON CONFLICT (subtest_id, codigo_item) DO UPDATE SET
    tipo_item = EXCLUDED.tipo_item,
    tipo_respuesta = EXCLUDED.tipo_respuesta,
    enunciado = EXCLUDED.enunciado,
    instruccion = EXCLUDED.instruccion,
    numero_orden = EXCLUDED.numero_orden,
    puntaje_base = EXCLUDED.puntaje_base,
    es_obligatorio = EXCLUDED.es_obligatorio,
    es_confidencial = EXCLUDED.es_confidencial,
    estado = EXCLUDED.estado;

INSERT INTO opcion_item (
    item_id,
    codigo_opcion,
    texto_opcion,
    numero_orden,
    valor_ordinal,
    estado
)
SELECT
    item.item_id,
    option_seed.codigo,
    option_seed.texto,
    option_seed.numero_orden,
    option_seed.numero_orden,
    'ACTIVO'
FROM item
JOIN subtest subtest ON subtest.subtest_id = item.subtest_id
JOIN version_test version ON version.version_test_id = subtest.version_test_id
JOIN test test ON test.test_id = version.test_id
CROSS JOIN (
    VALUES
        ('A', 'Opcion A', 1),
        ('B', 'Opcion B', 2),
        ('C', 'Opcion C', 3),
        ('D', 'Opcion D', 4)
) AS option_seed(codigo, texto, numero_orden)
WHERE test.codigo_test = 'BFA'
  AND version.numero_version IN ('2.1', '2.2')
ON CONFLICT (item_id, codigo_opcion) DO UPDATE SET
    texto_opcion = EXCLUDED.texto_opcion,
    numero_orden = EXCLUDED.numero_orden,
    valor_ordinal = EXCLUDED.valor_ordinal,
    estado = EXCLUDED.estado;

INSERT INTO regla_calificacion (
    version_test_id,
    subtest_id,
    item_id,
    estrategia_calificacion_id,
    tipo_regla,
    prioridad,
    activa,
    estado,
    parametros,
    observacion,
    creado_por,
    creado_en,
    aprobado_por,
    aprobado_en
)
SELECT
    version.version_test_id,
    subtest.subtest_id,
    item.item_id,
    (SELECT estrategia_calificacion_id FROM estrategia_calificacion WHERE codigo = 'CLAVE_SIMPLE'),
    'CLAVE_ITEM',
    1,
    TRUE,
    CASE
        WHEN version.estado = 'PUBLICADO' THEN 'PUBLICADO'::estado_configuracion
        ELSE 'BORRADOR'::estado_configuracion
    END,
    '{}'::jsonb,
    'Regla de clave simple generada por seed BFA.',
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    version.creado_en,
    CASE WHEN version.estado = 'PUBLICADO' THEN (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1) ELSE NULL END,
    CASE WHEN version.estado = 'PUBLICADO' THEN version.aprobado_en ELSE NULL END
FROM item
JOIN subtest subtest ON subtest.subtest_id = item.subtest_id
JOIN version_test version ON version.version_test_id = subtest.version_test_id
JOIN test test ON test.test_id = version.test_id
WHERE test.codigo_test = 'BFA'
  AND version.numero_version IN ('2.1', '2.2')
  AND NOT EXISTS (
      SELECT 1
      FROM regla_calificacion existing
      WHERE existing.item_id = item.item_id
        AND existing.tipo_regla = 'CLAVE_ITEM'
        AND existing.activa = TRUE
  );

INSERT INTO clave_respuesta (
    regla_calificacion_id,
    item_id,
    opcion_correcta_id,
    texto_esperado,
    valor_numerico_esperado,
    tolerancia_numerica,
    puntaje,
    requiere_revision_manual,
    creado_por,
    creado_en
)
SELECT
    rule.regla_calificacion_id,
    item.item_id,
    option.opcion_id,
    NULL,
    NULL,
    NULL,
    1.00,
    FALSE,
    (SELECT usuario_id FROM usuario WHERE nombre_usuario = 'admin' LIMIT 1),
    rule.creado_en
FROM item
JOIN subtest subtest ON subtest.subtest_id = item.subtest_id
JOIN version_test version ON version.version_test_id = subtest.version_test_id
JOIN test test ON test.test_id = version.test_id
JOIN regla_calificacion rule ON rule.item_id = item.item_id
JOIN opcion_item option ON option.item_id = item.item_id
    AND option.numero_orden = ((item.numero_orden - 1) % 4) + 1
WHERE test.codigo_test = 'BFA'
  AND version.numero_version IN ('2.1', '2.2')
  AND rule.tipo_regla = 'CLAVE_ITEM'
  AND rule.activa = TRUE
ON CONFLICT (regla_calificacion_id, item_id) DO UPDATE SET
    opcion_correcta_id = EXCLUDED.opcion_correcta_id,
    puntaje = EXCLUDED.puntaje,
    requiere_revision_manual = EXCLUDED.requiere_revision_manual;
