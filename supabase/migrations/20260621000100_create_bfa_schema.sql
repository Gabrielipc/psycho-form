CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ============================================================
-- Limpieza destructiva omitida: esta es una migracion inicial no repetible.

-- ============================================================
-- TIPOS Y CATALOGOS DE ESTADO
-- ============================================================

CREATE TYPE estado_general AS ENUM ('ACTIVO', 'INACTIVO');

CREATE TYPE estado_configuracion AS ENUM (
    'BORRADOR',
    'EN_REVISION',
    'APROBADO',
    'PUBLICADO',
    'RETIRADO'
);

CREATE TYPE estado_version_test AS ENUM (
    'BORRADOR',
    'EN_REVISION',
    'APROBADO',
    'PUBLICADO',
    'RETIRADO'
);

CREATE TYPE tipo_item AS ENUM (
    'SOLO_TEXTO',
    'SOLO_IMAGEN',
    'TEXTO_E_IMAGEN',
    'COMPARACION_IMAGENES',
    'RAZONAMIENTO_VERBAL'
);

CREATE TYPE tipo_respuesta AS ENUM (
    'OPCION_UNICA',
    'OPCION_MULTIPLE',
    'TEXTO_ABIERTO',
    'NUMERICA',
    'VERDADERO_FALSO'
);

CREATE TYPE estado_sesion_aplicacion AS ENUM (
    'PROGRAMADA',
    'ABIERTA',
    'CERRADA',
    'CANCELADA'
);

CREATE TYPE estado_asignacion AS ENUM (
    'ASIGNADO',
    'EN_PROGRESO',
    'COMPLETADO',
    'ANULADO',
    'EXPIRADO'
);

CREATE TYPE estado_intento AS ENUM (
    'NO_INICIADO',
    'EN_PROGRESO',
    'COMPLETADO',
    'INTERRUMPIDO',
    'ANULADO'
);

CREATE TYPE tipo_recurso AS ENUM (
    'IMAGEN',
    'DOCUMENTO',
    'OTRO'
);

CREATE TYPE formato_reporte AS ENUM (
    'PDF',
    'XLSX',
    'CSV'
);

CREATE TYPE tipo_estrategia_calificacion AS ENUM (
    'CLAVE_SIMPLE',
    'MATRIZ_DIMENSIONAL',
    'LIKERT_ESCALA',
    'RESPUESTA_NUMERICA',
    'REVISION_MANUAL',
    'RUBRICA_MANUAL',
    'FORMULA_CONTROLADA'
);

CREATE TYPE tipo_regla_calificacion AS ENUM (
    'CLAVE_ITEM',
    'OPCION_DIMENSION',
    'LIKERT',
    'NUMERICA_RANGO',
    'REVISION_MANUAL',
    'RUBRICA',
    'FORMULA'
);

CREATE TYPE estado_revision_manual AS ENUM (
    'PENDIENTE',
    'EN_REVISION',
    'REVISADO',
    'OBSERVADO',
    'ANULADO'
);

-- ============================================================
-- SEGURIDAD
-- ============================================================

CREATE TABLE usuario (
    usuario_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_usuario VARCHAR(80) NOT NULL UNIQUE,
    correo VARCHAR(150) NOT NULL UNIQUE,
    hash_contrasena VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rol (
    rol_id SMALLSERIAL PRIMARY KEY,
    nombre_rol VARCHAR(60) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE permiso (
    permiso_id SMALLSERIAL PRIMARY KEY,
    codigo_permiso VARCHAR(80) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuario_rol (
    usuario_id UUID NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    rol_id SMALLINT NOT NULL REFERENCES rol(rol_id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE rol_permiso (
    rol_id SMALLINT NOT NULL REFERENCES rol(rol_id) ON DELETE CASCADE,
    permiso_id SMALLINT NOT NULL REFERENCES permiso(permiso_id) ON DELETE CASCADE,
    PRIMARY KEY (rol_id, permiso_id)
);

-- ============================================================
-- CATALOGOS ACADEMICOS Y DEMOGRAFICOS
-- ============================================================

CREATE TABLE catalogo_sexo (
    sexo_id SMALLSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(80) NOT NULL,
    estado estado_general NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE carrera (
    carrera_id SMALLSERIAL PRIMARY KEY,
    codigo_carrera VARCHAR(30) NOT NULL UNIQUE,
    nombre_carrera VARCHAR(150) NOT NULL,
    estado estado_general NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE cohorte (
    cohorte_id SMALLSERIAL PRIMARY KEY,
    codigo_cohorte VARCHAR(30) NOT NULL UNIQUE,
    nombre_cohorte VARCHAR(100) NOT NULL,
    anio SMALLINT CHECK (anio IS NULL OR anio BETWEEN 1900 AND 2100),
    periodo VARCHAR(30),
    estado estado_general NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE grupo_academico (
    grupo_academico_id SMALLSERIAL PRIMARY KEY,
    carrera_id SMALLINT REFERENCES carrera(carrera_id) ON DELETE RESTRICT,
    codigo_grupo VARCHAR(50) NOT NULL,
    nombre_grupo VARCHAR(100),
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    UNIQUE (carrera_id, codigo_grupo)
);

-- ============================================================
-- PARTICIPANTES
-- ============================================================

CREATE TABLE participante (
    participante_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_participante VARCHAR(80) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    sexo_id SMALLINT REFERENCES catalogo_sexo(sexo_id) ON DELETE RESTRICT,
    carrera_id SMALLINT REFERENCES carrera(carrera_id) ON DELETE RESTRICT,
    cohorte_id SMALLINT REFERENCES cohorte(cohorte_id) ON DELETE RESTRICT,
    grupo_academico_id SMALLINT REFERENCES grupo_academico(grupo_academico_id) ON DELETE RESTRICT,
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ESTRATEGIAS DE CALIFICACION
-- ============================================================

CREATE TABLE estrategia_calificacion (
    estrategia_calificacion_id SMALLSERIAL PRIMARY KEY,
    codigo VARCHAR(60) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    tipo_estrategia tipo_estrategia_calificacion NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    permite_baremo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_estrategia_tipo_codigo UNIQUE (tipo_estrategia, codigo)
);

-- ============================================================
-- TESTS, VERSIONES, SUBTESTS, DIMENSIONES E ITEMS
-- ============================================================

CREATE TABLE test (
    test_id BIGSERIAL PRIMARY KEY,
    codigo_test VARCHAR(50) NOT NULL UNIQUE,
    nombre_test VARCHAR(150) NOT NULL,
    descripcion TEXT,
    creado_por UUID REFERENCES usuario(usuario_id),
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE version_test (
    version_test_id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL REFERENCES test(test_id) ON DELETE CASCADE,
    estrategia_calificacion_id SMALLINT REFERENCES estrategia_calificacion(estrategia_calificacion_id) ON DELETE RESTRICT,
    numero_version VARCHAR(30) NOT NULL,
    estado estado_version_test NOT NULL DEFAULT 'BORRADOR',
    instrucciones_generales TEXT,
    tiempo_limite_segundos INT CHECK (tiempo_limite_segundos IS NULL OR tiempo_limite_segundos > 0),
    permite_aleatorizar_subtests BOOLEAN NOT NULL DEFAULT FALSE,
    permite_aleatorizar_items BOOLEAN NOT NULL DEFAULT FALSE,
    aprobado_por UUID REFERENCES usuario(usuario_id),
    aprobado_en TIMESTAMP,
    publicado_en TIMESTAMP,
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (test_id, numero_version),
    CONSTRAINT ck_version_aprobacion CHECK (
        estado NOT IN ('APROBADO', 'PUBLICADO')
        OR (aprobado_por IS NOT NULL AND aprobado_en IS NOT NULL)
    ),
    CONSTRAINT ck_version_publicacion CHECK (
        estado <> 'PUBLICADO'
        OR (publicado_en IS NOT NULL AND estrategia_calificacion_id IS NOT NULL)
    )
);

CREATE TABLE subtest (
    subtest_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE CASCADE,
    estrategia_calificacion_id SMALLINT REFERENCES estrategia_calificacion(estrategia_calificacion_id) ON DELETE RESTRICT,
    codigo_subtest VARCHAR(50) NOT NULL,
    nombre_subtest VARCHAR(150) NOT NULL,
    descripcion TEXT,
    instrucciones TEXT,
    numero_orden INT NOT NULL,
    tiempo_limite_segundos INT CHECK (tiempo_limite_segundos IS NULL OR tiempo_limite_segundos > 0),
    permite_aleatorizar_items BOOLEAN NOT NULL DEFAULT FALSE,
    permite_aleatorizar_opciones BOOLEAN NOT NULL DEFAULT FALSE,
    es_obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    UNIQUE (version_test_id, codigo_subtest),
    UNIQUE (version_test_id, numero_orden),
    UNIQUE (version_test_id, subtest_id)
);

CREATE TABLE dimension_resultado (
    dimension_resultado_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE CASCADE,
    subtest_id BIGINT,
    codigo_dimension VARCHAR(60) NOT NULL,
    nombre_dimension VARCHAR(150) NOT NULL,
    descripcion TEXT,
    orden_presentacion INT NOT NULL,
    puntaje_minimo DECIMAL(10,2),
    puntaje_maximo DECIMAL(10,2),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (version_test_id, codigo_dimension),
    UNIQUE (version_test_id, orden_presentacion),
    UNIQUE (version_test_id, dimension_resultado_id),
    FOREIGN KEY (version_test_id, subtest_id)
        REFERENCES subtest(version_test_id, subtest_id) ON DELETE CASCADE,
    CONSTRAINT ck_dimension_rango CHECK (
        puntaje_minimo IS NULL
        OR puntaje_maximo IS NULL
        OR puntaje_minimo <= puntaje_maximo
    )
);

CREATE TABLE item (
    item_id BIGSERIAL PRIMARY KEY,
    subtest_id BIGINT NOT NULL REFERENCES subtest(subtest_id) ON DELETE CASCADE,
    codigo_item VARCHAR(80) NOT NULL,
    tipo_item tipo_item NOT NULL,
    tipo_respuesta tipo_respuesta NOT NULL,
    enunciado TEXT,
    instruccion TEXT,
    numero_orden INT NOT NULL,
    puntaje_base DECIMAL(8,2) NOT NULL DEFAULT 1.00 CHECK (puntaje_base >= 0),
    tiempo_limite_segundos INT CHECK (tiempo_limite_segundos IS NULL OR tiempo_limite_segundos > 0),
    es_obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    es_confidencial BOOLEAN NOT NULL DEFAULT TRUE,
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    UNIQUE (subtest_id, codigo_item),
    UNIQUE (subtest_id, numero_orden),
    UNIQUE (subtest_id, item_id)
);

-- ============================================================
-- RECURSOS MULTIMEDIA
-- ============================================================

CREATE TABLE recurso_multimedia (
    recurso_id BIGSERIAL PRIMARY KEY,
    tipo_recurso tipo_recurso NOT NULL DEFAULT 'IMAGEN',
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_almacenamiento TEXT NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamano_bytes BIGINT CHECK (tamano_bytes IS NULL OR tamano_bytes >= 0),
    hash_integridad VARCHAR(128),
    es_confidencial BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_autorizacion BOOLEAN NOT NULL DEFAULT TRUE,
    subido_por UUID REFERENCES usuario(usuario_id),
    subido_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE imagen_item (
    imagen_item_id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES item(item_id) ON DELETE CASCADE,
    recurso_id BIGINT NOT NULL REFERENCES recurso_multimedia(recurso_id) ON DELETE RESTRICT,
    rol_imagen VARCHAR(50) NOT NULL DEFAULT 'ENUNCIADO',
    numero_orden INT NOT NULL DEFAULT 1,
    texto_alternativo VARCHAR(255),
    UNIQUE (item_id, recurso_id),
    UNIQUE (item_id, numero_orden)
);

CREATE TABLE opcion_item (
    opcion_id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL REFERENCES item(item_id) ON DELETE CASCADE,
    codigo_opcion VARCHAR(50) NOT NULL,
    texto_opcion TEXT,
    numero_orden INT NOT NULL,
    valor_ordinal DECIMAL(8,2),
    estado estado_general NOT NULL DEFAULT 'ACTIVO',
    UNIQUE (item_id, codigo_opcion),
    UNIQUE (item_id, numero_orden),
    UNIQUE (item_id, opcion_id)
);

CREATE TABLE imagen_opcion (
    imagen_opcion_id BIGSERIAL PRIMARY KEY,
    opcion_id BIGINT NOT NULL REFERENCES opcion_item(opcion_id) ON DELETE CASCADE,
    recurso_id BIGINT NOT NULL REFERENCES recurso_multimedia(recurso_id) ON DELETE RESTRICT,
    numero_orden INT NOT NULL DEFAULT 1,
    texto_alternativo VARCHAR(255),
    UNIQUE (opcion_id, recurso_id),
    UNIQUE (opcion_id, numero_orden)
);

-- ============================================================
-- REGLAS, CLAVES Y MATRICES DE PUNTAJE
-- ============================================================

CREATE TABLE regla_calificacion (
    regla_calificacion_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE CASCADE,
    subtest_id BIGINT,
    item_id BIGINT,
    estrategia_calificacion_id SMALLINT NOT NULL REFERENCES estrategia_calificacion(estrategia_calificacion_id) ON DELETE RESTRICT,
    tipo_regla tipo_regla_calificacion NOT NULL,
    prioridad INT NOT NULL DEFAULT 1 CHECK (prioridad > 0),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    estado estado_configuracion NOT NULL DEFAULT 'BORRADOR',
    parametros JSONB NOT NULL DEFAULT '{}'::jsonb,
    observacion TEXT,
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aprobado_por UUID REFERENCES usuario(usuario_id),
    aprobado_en TIMESTAMP,
    FOREIGN KEY (version_test_id, subtest_id)
        REFERENCES subtest(version_test_id, subtest_id) ON DELETE CASCADE,
    FOREIGN KEY (subtest_id, item_id)
        REFERENCES item(subtest_id, item_id) ON DELETE CASCADE,
    CONSTRAINT ck_regla_item_requiere_subtest CHECK (
        item_id IS NULL OR subtest_id IS NOT NULL
    ),
    CONSTRAINT ck_regla_aprobacion CHECK (
        estado NOT IN ('APROBADO', 'PUBLICADO')
        OR (aprobado_por IS NOT NULL AND aprobado_en IS NOT NULL)
    )
);

CREATE TABLE clave_respuesta (
    clave_respuesta_id BIGSERIAL PRIMARY KEY,
    regla_calificacion_id BIGINT NOT NULL REFERENCES regla_calificacion(regla_calificacion_id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES item(item_id) ON DELETE CASCADE,
    opcion_correcta_id BIGINT,
    texto_esperado TEXT,
    valor_numerico_esperado DECIMAL(12,4),
    tolerancia_numerica DECIMAL(12,4) CHECK (tolerancia_numerica IS NULL OR tolerancia_numerica >= 0),
    puntaje DECIMAL(8,2) NOT NULL DEFAULT 1.00 CHECK (puntaje >= 0),
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id, opcion_correcta_id)
        REFERENCES opcion_item(item_id, opcion_id) ON DELETE RESTRICT,
    CONSTRAINT ck_clave_tipo CHECK (
        opcion_correcta_id IS NOT NULL
        OR texto_esperado IS NOT NULL
        OR valor_numerico_esperado IS NOT NULL
        OR requiere_revision_manual = TRUE
    ),
    UNIQUE (regla_calificacion_id, item_id)
);

CREATE TABLE opcion_puntaje_dimension (
    opcion_puntaje_dimension_id BIGSERIAL PRIMARY KEY,
    regla_calificacion_id BIGINT NOT NULL REFERENCES regla_calificacion(regla_calificacion_id) ON DELETE CASCADE,
    opcion_id BIGINT NOT NULL REFERENCES opcion_item(opcion_id) ON DELETE CASCADE,
    dimension_resultado_id BIGINT NOT NULL REFERENCES dimension_resultado(dimension_resultado_id) ON DELETE CASCADE,
    puntaje DECIMAL(8,2) NOT NULL DEFAULT 0 CHECK (puntaje >= 0),
    peso DECIMAL(8,4) NOT NULL DEFAULT 1 CHECK (peso > 0),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_opcion_dimension_activa
    ON opcion_puntaje_dimension(opcion_id, dimension_resultado_id)
    WHERE activa;

-- ============================================================
-- BAREMOS VERSIONADOS
-- ============================================================

CREATE TABLE baremo (
    baremo_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE CASCADE,
    dimension_resultado_id BIGINT,
    codigo_baremo VARCHAR(60) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    grupo_normativo VARCHAR(150),
    criterio_edad_minima SMALLINT CHECK (criterio_edad_minima IS NULL OR criterio_edad_minima BETWEEN 0 AND 120),
    criterio_edad_maxima SMALLINT CHECK (criterio_edad_maxima IS NULL OR criterio_edad_maxima BETWEEN 0 AND 120),
    criterio_sexo_id SMALLINT REFERENCES catalogo_sexo(sexo_id) ON DELETE RESTRICT,
    criterio_carrera_id SMALLINT REFERENCES carrera(carrera_id) ON DELETE RESTRICT,
    vigente_desde DATE,
    vigente_hasta DATE,
    estado estado_configuracion NOT NULL DEFAULT 'BORRADOR',
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aprobado_por UUID REFERENCES usuario(usuario_id),
    aprobado_en TIMESTAMP,
    FOREIGN KEY (version_test_id, dimension_resultado_id)
        REFERENCES dimension_resultado(version_test_id, dimension_resultado_id) ON DELETE CASCADE,
    CONSTRAINT ck_baremo_edad CHECK (
        criterio_edad_minima IS NULL
        OR criterio_edad_maxima IS NULL
        OR criterio_edad_minima <= criterio_edad_maxima
    ),
    CONSTRAINT ck_baremo_vigencia CHECK (
        vigente_desde IS NULL OR vigente_hasta IS NULL OR vigente_hasta >= vigente_desde
    ),
    CONSTRAINT ck_baremo_aprobacion CHECK (
        estado NOT IN ('APROBADO', 'PUBLICADO')
        OR (aprobado_por IS NOT NULL AND aprobado_en IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_baremo_dimension_codigo
    ON baremo(version_test_id, dimension_resultado_id, codigo_baremo)
    WHERE dimension_resultado_id IS NOT NULL;

CREATE UNIQUE INDEX uq_baremo_total_codigo
    ON baremo(version_test_id, codigo_baremo)
    WHERE dimension_resultado_id IS NULL;

CREATE TABLE rango_baremo (
    rango_baremo_id BIGSERIAL PRIMARY KEY,
    baremo_id BIGINT NOT NULL REFERENCES baremo(baremo_id) ON DELETE CASCADE,
    puntaje_minimo DECIMAL(10,2) NOT NULL,
    puntaje_maximo DECIMAL(10,2) NOT NULL,
    percentil DECIMAL(6,2) CHECK (percentil IS NULL OR percentil BETWEEN 0 AND 100),
    categoria VARCHAR(100) NOT NULL,
    interpretacion TEXT,
    recomendacion TEXT,
    orden INT NOT NULL,
    UNIQUE (baremo_id, orden),
    UNIQUE (baremo_id, rango_baremo_id),
    CONSTRAINT ck_rango_baremo_puntaje CHECK (puntaje_minimo <= puntaje_maximo),
    CONSTRAINT ex_rango_baremo_no_solapado EXCLUDE USING gist (
        baremo_id WITH =,
        numrange(puntaje_minimo, puntaje_maximo, '[]') WITH &&
    )
);

-- ============================================================
-- RUBRICAS Y REVISION MANUAL ESTRUCTURADA
-- ============================================================

CREATE TABLE rubrica_evaluacion (
    rubrica_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE CASCADE,
    subtest_id BIGINT,
    item_id BIGINT,
    codigo_rubrica VARCHAR(60) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    puntaje_maximo DECIMAL(8,2) CHECK (puntaje_maximo IS NULL OR puntaje_maximo >= 0),
    estado estado_configuracion NOT NULL DEFAULT 'BORRADOR',
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aprobado_por UUID REFERENCES usuario(usuario_id),
    aprobado_en TIMESTAMP,
    FOREIGN KEY (version_test_id, subtest_id)
        REFERENCES subtest(version_test_id, subtest_id) ON DELETE CASCADE,
    FOREIGN KEY (subtest_id, item_id)
        REFERENCES item(subtest_id, item_id) ON DELETE CASCADE,
    UNIQUE (version_test_id, codigo_rubrica),
    CONSTRAINT ck_rubrica_item_requiere_subtest CHECK (
        item_id IS NULL OR subtest_id IS NOT NULL
    ),
    CONSTRAINT ck_rubrica_aprobacion CHECK (
        estado NOT IN ('APROBADO', 'PUBLICADO')
        OR (aprobado_por IS NOT NULL AND aprobado_en IS NOT NULL)
    )
);

CREATE TABLE criterio_rubrica (
    criterio_rubrica_id BIGSERIAL PRIMARY KEY,
    rubrica_id BIGINT NOT NULL REFERENCES rubrica_evaluacion(rubrica_id) ON DELETE CASCADE,
    codigo_criterio VARCHAR(60) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    puntaje_maximo DECIMAL(8,2) NOT NULL CHECK (puntaje_maximo >= 0),
    peso DECIMAL(8,4) NOT NULL DEFAULT 1 CHECK (peso > 0),
    orden INT NOT NULL,
    UNIQUE (rubrica_id, codigo_criterio),
    UNIQUE (rubrica_id, orden)
);

CREATE TABLE nivel_criterio_rubrica (
    nivel_criterio_rubrica_id BIGSERIAL PRIMARY KEY,
    criterio_rubrica_id BIGINT NOT NULL REFERENCES criterio_rubrica(criterio_rubrica_id) ON DELETE CASCADE,
    codigo_nivel VARCHAR(60) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    puntaje DECIMAL(8,2) NOT NULL CHECK (puntaje >= 0),
    orden INT NOT NULL,
    UNIQUE (criterio_rubrica_id, codigo_nivel),
    UNIQUE (criterio_rubrica_id, orden)
);

-- ============================================================
-- SESIONES CONFIGURABLES Y TOKENS
-- ============================================================

CREATE TABLE sesion_aplicacion (
    sesion_aplicacion_id BIGSERIAL PRIMARY KEY,
    version_test_id BIGINT NOT NULL REFERENCES version_test(version_test_id) ON DELETE RESTRICT,
    codigo_sesion VARCHAR(80) NOT NULL UNIQUE,
    nombre_sesion VARCHAR(150) NOT NULL,
    descripcion TEXT,
    inicio_programado TIMESTAMP NOT NULL,
    fin_programado TIMESTAMP,
    inicio_real TIMESTAMP,
    fin_real TIMESTAMP,
    ubicacion VARCHAR(150),
    estado estado_sesion_aplicacion NOT NULL DEFAULT 'PROGRAMADA',
    creado_por UUID REFERENCES usuario(usuario_id),
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sesion_aplicacion_id, version_test_id),
    CONSTRAINT ck_fechas_sesion CHECK (
        fin_programado IS NULL OR fin_programado > inicio_programado
    )
);

CREATE TABLE sesion_subtest (
    sesion_subtest_id BIGSERIAL PRIMARY KEY,
    sesion_aplicacion_id BIGINT NOT NULL,
    version_test_id BIGINT NOT NULL,
    subtest_id BIGINT NOT NULL,
    numero_orden INT NOT NULL,
    tiempo_limite_segundos INT CHECK (tiempo_limite_segundos IS NULL OR tiempo_limite_segundos > 0),
    permite_aleatorizar_items BOOLEAN NOT NULL DEFAULT FALSE,
    permite_aleatorizar_opciones BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sesion_aplicacion_id, version_test_id)
        REFERENCES sesion_aplicacion(sesion_aplicacion_id, version_test_id) ON DELETE CASCADE,
    FOREIGN KEY (version_test_id, subtest_id)
        REFERENCES subtest(version_test_id, subtest_id) ON DELETE RESTRICT,
    UNIQUE (sesion_aplicacion_id, subtest_id),
    UNIQUE (sesion_aplicacion_id, numero_orden)
);

CREATE TABLE asignacion_test (
    asignacion_id BIGSERIAL PRIMARY KEY,
    sesion_aplicacion_id BIGINT NOT NULL REFERENCES sesion_aplicacion(sesion_aplicacion_id) ON DELETE CASCADE,
    participante_id UUID NOT NULL REFERENCES participante(participante_id) ON DELETE RESTRICT,
    evaluador_id UUID NOT NULL REFERENCES usuario(usuario_id) ON DELETE RESTRICT,
    token_acceso_hash VARCHAR(255) NOT NULL UNIQUE,
    token_expira_en TIMESTAMP NOT NULL,
    token_usado_en TIMESTAMP,
    intentos_acceso INT NOT NULL DEFAULT 0 CHECK (intentos_acceso >= 0),
    edad_registrada_aplicacion SMALLINT CHECK (edad_registrada_aplicacion IS NULL OR edad_registrada_aplicacion BETWEEN 0 AND 120),
    sexo_id_aplicacion SMALLINT REFERENCES catalogo_sexo(sexo_id) ON DELETE RESTRICT,
    carrera_id_aplicacion SMALLINT REFERENCES carrera(carrera_id) ON DELETE RESTRICT,
    cohorte_id_aplicacion SMALLINT REFERENCES cohorte(cohorte_id) ON DELETE RESTRICT,
    grupo_academico_id_aplicacion SMALLINT REFERENCES grupo_academico(grupo_academico_id) ON DELETE RESTRICT,
    asignado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado estado_asignacion NOT NULL DEFAULT 'ASIGNADO',
    UNIQUE (sesion_aplicacion_id, participante_id)
);

CREATE TABLE intento_test (
    intento_id BIGSERIAL PRIMARY KEY,
    asignacion_id BIGINT NOT NULL UNIQUE REFERENCES asignacion_test(asignacion_id) ON DELETE CASCADE,
    iniciado_en TIMESTAMP,
    finalizado_en TIMESTAMP,
    estado estado_intento NOT NULL DEFAULT 'NO_INICIADO',
    ultimo_subtest_id BIGINT REFERENCES subtest(subtest_id),
    ultima_actividad_en TIMESTAMP,
    tiempo_total_segundos INT CHECK (tiempo_total_segundos IS NULL OR tiempo_total_segundos >= 0),
    informacion_dispositivo TEXT,
    direccion_ip VARCHAR(60)
);

CREATE TABLE intento_subtest (
    intento_subtest_id BIGSERIAL PRIMARY KEY,
    intento_id BIGINT NOT NULL REFERENCES intento_test(intento_id) ON DELETE CASCADE,
    sesion_subtest_id BIGINT NOT NULL REFERENCES sesion_subtest(sesion_subtest_id) ON DELETE RESTRICT,
    subtest_id BIGINT NOT NULL REFERENCES subtest(subtest_id) ON DELETE RESTRICT,
    iniciado_en TIMESTAMP,
    finalizado_en TIMESTAMP,
    estado estado_intento NOT NULL DEFAULT 'NO_INICIADO',
    tiempo_usado_segundos INT CHECK (tiempo_usado_segundos IS NULL OR tiempo_usado_segundos >= 0),
    UNIQUE (intento_id, subtest_id)
);

-- ============================================================
-- RESPUESTAS FINALES DEL PARTICIPANTE
-- ============================================================

CREATE TABLE respuesta_item (
    respuesta_id BIGSERIAL PRIMARY KEY,
    intento_id BIGINT NOT NULL REFERENCES intento_test(intento_id) ON DELETE CASCADE,
    intento_subtest_id BIGINT REFERENCES intento_subtest(intento_subtest_id) ON DELETE CASCADE,
    item_id BIGINT NOT NULL REFERENCES item(item_id) ON DELETE RESTRICT,
    respuesta_texto_abierto TEXT,
    respuesta_numerica DECIMAL(12,4),
    respondido_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tiempo_usado_segundos INT CHECK (tiempo_usado_segundos IS NULL OR tiempo_usado_segundos >= 0),
    es_final BOOLEAN NOT NULL DEFAULT TRUE,
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (intento_id, item_id),
    UNIQUE (respuesta_id, item_id)
);

CREATE TABLE opcion_seleccionada_respuesta (
    respuesta_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    opcion_id BIGINT NOT NULL,
    seleccionada_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (respuesta_id, opcion_id),
    FOREIGN KEY (respuesta_id, item_id)
        REFERENCES respuesta_item(respuesta_id, item_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id, opcion_id)
        REFERENCES opcion_item(item_id, opcion_id) ON DELETE RESTRICT
);

-- ============================================================
-- RESULTADOS, CALIFICACION DETALLADA Y BAREMOS APLICADOS
-- ============================================================

CREATE TABLE resultado (
    resultado_id BIGSERIAL PRIMARY KEY,
    intento_id BIGINT NOT NULL UNIQUE REFERENCES intento_test(intento_id) ON DELETE CASCADE,
    estrategia_calificacion_id SMALLINT REFERENCES estrategia_calificacion(estrategia_calificacion_id) ON DELETE RESTRICT,
    calculado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    calculado_por UUID REFERENCES usuario(usuario_id),
    puntaje_total_directo DECIMAL(10,2) NOT NULL DEFAULT 0,
    cantidad_items INT NOT NULL DEFAULT 0 CHECK (cantidad_items >= 0),
    cantidad_correctas INT NOT NULL DEFAULT 0 CHECK (cantidad_correctas >= 0),
    cantidad_incorrectas INT NOT NULL DEFAULT 0 CHECK (cantidad_incorrectas >= 0),
    cantidad_pendientes_revision INT NOT NULL DEFAULT 0 CHECK (cantidad_pendientes_revision >= 0),
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    estado VARCHAR(30) NOT NULL DEFAULT 'CALCULADO'
);

CREATE TABLE resultado_dimension (
    resultado_dimension_id BIGSERIAL PRIMARY KEY,
    resultado_id BIGINT NOT NULL REFERENCES resultado(resultado_id) ON DELETE CASCADE,
    dimension_resultado_id BIGINT NOT NULL REFERENCES dimension_resultado(dimension_resultado_id) ON DELETE RESTRICT,
    baremo_id BIGINT REFERENCES baremo(baremo_id) ON DELETE RESTRICT,
    rango_baremo_id BIGINT,
    puntaje_directo DECIMAL(10,2) NOT NULL DEFAULT 0,
    puntaje_transformado DECIMAL(10,2),
    percentil DECIMAL(6,2) CHECK (percentil IS NULL OR percentil BETWEEN 0 AND 100),
    categoria VARCHAR(100),
    interpretacion TEXT,
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    observacion TEXT,
    UNIQUE (resultado_id, dimension_resultado_id),
    CONSTRAINT ck_resultado_dimension_rango_baremo CHECK (
        rango_baremo_id IS NULL OR baremo_id IS NOT NULL
    ),
    FOREIGN KEY (baremo_id, rango_baremo_id)
        REFERENCES rango_baremo(baremo_id, rango_baremo_id) ON DELETE RESTRICT
);

CREATE TABLE calificacion_respuesta (
    calificacion_respuesta_id BIGSERIAL PRIMARY KEY,
    resultado_id BIGINT NOT NULL REFERENCES resultado(resultado_id) ON DELETE CASCADE,
    respuesta_id BIGINT NOT NULL REFERENCES respuesta_item(respuesta_id) ON DELETE CASCADE,
    regla_calificacion_id BIGINT REFERENCES regla_calificacion(regla_calificacion_id) ON DELETE RESTRICT,
    dimension_resultado_id BIGINT REFERENCES dimension_resultado(dimension_resultado_id) ON DELETE RESTRICT,
    opcion_id BIGINT REFERENCES opcion_item(opcion_id) ON DELETE RESTRICT,
    puntaje_obtenido DECIMAL(10,2) NOT NULL DEFAULT 0,
    es_correcta BOOLEAN,
    requiere_revision_manual BOOLEAN NOT NULL DEFAULT FALSE,
    observacion TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE revision_manual_respuesta (
    revision_manual_respuesta_id BIGSERIAL PRIMARY KEY,
    respuesta_id BIGINT NOT NULL REFERENCES respuesta_item(respuesta_id) ON DELETE CASCADE,
    regla_calificacion_id BIGINT REFERENCES regla_calificacion(regla_calificacion_id) ON DELETE RESTRICT,
    estado estado_revision_manual NOT NULL DEFAULT 'PENDIENTE',
    puntaje_asignado DECIMAL(10,2) CHECK (puntaje_asignado IS NULL OR puntaje_asignado >= 0),
    comentario TEXT,
    revisado_por UUID REFERENCES usuario(usuario_id),
    revisado_en TIMESTAMP,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (respuesta_id, regla_calificacion_id),
    CONSTRAINT ck_revision_manual_revisada CHECK (
        estado <> 'REVISADO'
        OR (revisado_por IS NOT NULL AND revisado_en IS NOT NULL)
    )
);

CREATE TABLE revision_rubrica_respuesta (
    revision_rubrica_respuesta_id BIGSERIAL PRIMARY KEY,
    revision_manual_respuesta_id BIGINT NOT NULL REFERENCES revision_manual_respuesta(revision_manual_respuesta_id) ON DELETE CASCADE,
    criterio_rubrica_id BIGINT NOT NULL REFERENCES criterio_rubrica(criterio_rubrica_id) ON DELETE RESTRICT,
    nivel_criterio_rubrica_id BIGINT REFERENCES nivel_criterio_rubrica(nivel_criterio_rubrica_id) ON DELETE RESTRICT,
    puntaje_asignado DECIMAL(10,2) NOT NULL CHECK (puntaje_asignado >= 0),
    comentario TEXT,
    UNIQUE (revision_manual_respuesta_id, criterio_rubrica_id)
);

-- Vista de compatibilidad para reportes heredados del MVP.
CREATE VIEW detalle_resultado AS
SELECT
    rd.resultado_dimension_id AS detalle_resultado_id,
    rd.resultado_id,
    dr.subtest_id,
    dr.nombre_dimension AS nombre_puntaje,
    rd.puntaje_directo,
    rd.puntaje_transformado,
    rd.percentil,
    rd.categoria,
    rd.interpretacion,
    rd.requiere_revision_manual,
    rd.observacion
FROM resultado_dimension rd
JOIN dimension_resultado dr
    ON dr.dimension_resultado_id = rd.dimension_resultado_id;

CREATE TABLE reporte_generado (
    reporte_id BIGSERIAL PRIMARY KEY,
    tipo_reporte VARCHAR(50) NOT NULL,
    intento_id BIGINT REFERENCES intento_test(intento_id) ON DELETE CASCADE,
    resultado_id BIGINT REFERENCES resultado(resultado_id) ON DELETE CASCADE,
    sesion_aplicacion_id BIGINT REFERENCES sesion_aplicacion(sesion_aplicacion_id) ON DELETE CASCADE,
    generado_por UUID REFERENCES usuario(usuario_id),
    generado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    formato formato_reporte NOT NULL,
    ruta_almacenamiento TEXT NOT NULL,
    resumen_filtros JSONB,
    CONSTRAINT ck_reporte_origen CHECK (
        intento_id IS NOT NULL OR resultado_id IS NOT NULL OR sesion_aplicacion_id IS NOT NULL
    )
);

-- ============================================================
-- AUDITORIA
-- ============================================================

CREATE TABLE auditoria (
    auditoria_id BIGSERIAL PRIMARY KEY,
    usuario_id UUID REFERENCES usuario(usuario_id),
    accion VARCHAR(100) NOT NULL,
    entidad VARCHAR(100) NOT NULL,
    entidad_id VARCHAR(100),
    valor_anterior JSONB,
    valor_nuevo JSONB,
    direccion_ip VARCHAR(60),
    agente_usuario TEXT,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDICES
-- ============================================================

CREATE INDEX idx_usuario_estado ON usuario(estado);
CREATE INDEX idx_participante_codigo ON participante(codigo_participante);
CREATE INDEX idx_participante_demografia ON participante(sexo_id, carrera_id, cohorte_id, grupo_academico_id);
CREATE INDEX idx_version_test_estado ON version_test(estado);
CREATE INDEX idx_subtest_version ON subtest(version_test_id);
CREATE INDEX idx_dimension_version ON dimension_resultado(version_test_id);
CREATE INDEX idx_item_subtest ON item(subtest_id);
CREATE INDEX idx_opcion_item ON opcion_item(item_id);
CREATE INDEX idx_recurso_confidencial ON recurso_multimedia(es_confidencial, requiere_autorizacion);
CREATE INDEX idx_regla_version_estado ON regla_calificacion(version_test_id, estado, activa);
CREATE INDEX idx_regla_item ON regla_calificacion(item_id);
CREATE INDEX idx_clave_item ON clave_respuesta(item_id);
CREATE INDEX idx_opcion_puntaje_dimension ON opcion_puntaje_dimension(dimension_resultado_id);
CREATE INDEX idx_baremo_dimension_estado ON baremo(dimension_resultado_id, estado);
CREATE INDEX idx_rango_baremo_baremo ON rango_baremo(baremo_id);
CREATE INDEX idx_sesion_estado ON sesion_aplicacion(estado);
CREATE INDEX idx_sesion_subtest_sesion ON sesion_subtest(sesion_aplicacion_id);
CREATE INDEX idx_asignacion_participante ON asignacion_test(participante_id);
CREATE INDEX idx_asignacion_evaluador ON asignacion_test(evaluador_id);
CREATE INDEX idx_asignacion_token_hash ON asignacion_test(token_acceso_hash);
CREATE INDEX idx_intento_asignacion ON intento_test(asignacion_id);
CREATE INDEX idx_intento_subtest_intento ON intento_subtest(intento_id);
CREATE INDEX idx_respuesta_intento ON respuesta_item(intento_id);
CREATE INDEX idx_respuesta_item ON respuesta_item(item_id);
CREATE INDEX idx_resultado_intento ON resultado(intento_id);
CREATE INDEX idx_resultado_dimension_resultado ON resultado_dimension(resultado_id);
CREATE INDEX idx_calificacion_resultado ON calificacion_respuesta(resultado_id);
CREATE INDEX idx_calificacion_respuesta ON calificacion_respuesta(respuesta_id);
CREATE INDEX idx_revision_manual_estado ON revision_manual_respuesta(estado);
CREATE INDEX idx_reporte_generado_fecha ON reporte_generado(generado_en);
CREATE INDEX idx_auditoria_usuario_fecha ON auditoria(usuario_id, creado_en);
CREATE INDEX idx_auditoria_entidad_fecha ON auditoria(entidad, entidad_id, creado_en);

-- ============================================================
-- DATOS BASE
-- ============================================================

INSERT INTO rol (nombre_rol, descripcion) VALUES
('ADMINISTRADOR', 'Administra usuarios, roles, configuracion general y auditoria'),
('PSICOLOGO_COORDINADOR', 'Configura instrumentos, valida resultados y consulta reportes'),
('APLICADOR', 'Crea sesiones de aplicacion y supervisa evaluaciones'),
('CONSULTOR_REPORTES', 'Consulta resultados y reportes autorizados');

INSERT INTO permiso (codigo_permiso, descripcion) VALUES
('USUARIO_GESTIONAR', 'Gestionar usuarios'),
('ROL_PERMISO_GESTIONAR', 'Gestionar roles y permisos'),
('TEST_CREAR', 'Crear tests, subtests, items y opciones'),
('TEST_PUBLICAR', 'Aprobar y publicar versiones de tests'),
('CALIFICACION_CONFIGURAR', 'Configurar estrategias, reglas, claves y matrices de puntaje'),
('BAREMO_CONFIGURAR', 'Configurar baremos y rangos de baremo'),
('RUBRICA_CONFIGURAR', 'Configurar rubricas de revision manual'),
('SESION_CREAR', 'Crear sesiones de aplicacion'),
('SESION_APLICAR', 'Aplicar y supervisar tests'),
('RESPUESTA_REVISAR', 'Revisar respuestas abiertas o manuales'),
('RESULTADO_VER', 'Consultar resultados'),
('REPORTE_EXPORTAR', 'Exportar reportes'),
('AUDITORIA_VER', 'Consultar auditoria');

INSERT INTO catalogo_sexo (codigo, nombre) VALUES
('F', 'Femenino'),
('M', 'Masculino'),
('ND', 'No declarado');

INSERT INTO estrategia_calificacion (
    codigo,
    nombre,
    descripcion,
    tipo_estrategia,
    requiere_revision_manual,
    permite_baremo
) VALUES
('CLAVE_SIMPLE', 'Clave simple', 'Compara la respuesta contra una clave aprobada y suma puntaje por acierto.', 'CLAVE_SIMPLE', FALSE, TRUE),
('MATRIZ_DIMENSIONAL', 'Matriz dimensional', 'Cada opcion seleccionada aporta puntaje a una o varias dimensiones de resultado.', 'MATRIZ_DIMENSIONAL', FALSE, TRUE),
('LIKERT_ESCALA', 'Escala Likert', 'Asigna valores ordinales a opciones y acumula o promedia puntajes por dimension.', 'LIKERT_ESCALA', FALSE, TRUE),
('RESPUESTA_NUMERICA', 'Respuesta numerica', 'Califica una respuesta numerica con valor esperado, rango o tolerancia.', 'RESPUESTA_NUMERICA', FALSE, TRUE),
('REVISION_MANUAL', 'Revision manual', 'Marca respuestas para revision manual por un usuario autorizado.', 'REVISION_MANUAL', TRUE, FALSE),
('RUBRICA_MANUAL', 'Rubrica manual', 'Permite calificar respuestas mediante criterios y niveles de rubrica.', 'RUBRICA_MANUAL', TRUE, TRUE),
('FORMULA_CONTROLADA', 'Formula controlada', 'Ejecuta calculos parametrizados bajo esquemas preaprobados por el backend.', 'FORMULA_CONTROLADA', FALSE, TRUE);

-- ============================================================
-- COMENTARIOS IMPORTANTES
-- ============================================================

COMMENT ON TABLE estrategia_calificacion IS
'Catalogo de estrategias de calificacion. Permite que el backend seleccione la logica general de calculo sin quemar reglas por test.';

COMMENT ON TABLE regla_calificacion IS
'Configura reglas aprobables por version, subtest o item. Las reglas no deben aplicarse en sesiones reales si no estan aprobadas/publicadas.';

COMMENT ON TABLE dimension_resultado IS
'Representa escalas, factores, subtests o categorias que reciben puntaje. Evita depender de nombres textuales repetidos en resultados.';

COMMENT ON TABLE opcion_puntaje_dimension IS
'Matriz opcion-dimension-puntaje para pruebas sin respuesta correcta unica, como valores, intereses o perfiles vocacionales.';

COMMENT ON TABLE baremo IS
'Baremo versionado y aprobable para transformar puntajes directos en categorias, percentiles o interpretaciones.';

COMMENT ON TABLE rango_baremo IS
'Rangos no solapados por baremo. La restriccion EXCLUDE evita que dos rangos de un mismo baremo cubran el mismo puntaje.';

COMMENT ON TABLE resultado_dimension IS
'Resultado calculado por dimension. Guarda el puntaje directo, transformado, categoria e interpretacion aplicada.';

COMMENT ON TABLE calificacion_respuesta IS
'Trazabilidad item a item entre respuesta, regla aplicada, opcion, dimension y puntaje obtenido.';

COMMENT ON TABLE revision_manual_respuesta IS
'Registra revision manual de respuestas abiertas o criterios no calificables automaticamente.';

COMMENT ON TABLE asignacion_test IS
'Acceso por link/token. Solo se guarda el hash del token, junto con snapshots demograficos relevantes para baremos historicos.';

COMMENT ON TABLE recurso_multimedia IS
'Los archivos reales deben estar en almacenamiento privado. Aqui solo se guarda ruta, hash y metadatos.';

COMMENT ON VIEW detalle_resultado IS
'Vista de compatibilidad del MVP. La fuente normalizada de resultados parciales es resultado_dimension.';
