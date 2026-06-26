begin;

select plan(10);

select has_table('public', 'usuario', 'La tabla usuario existe');
select has_type('public', 'estado_general', 'El enum estado_general existe');
select has_view('public', 'detalle_resultado', 'La vista detalle_resultado existe');
select results_eq('select count(*)::integer from rol', array[4], 'Se cargan cuatro roles base');
select results_eq('select count(*)::integer from permiso', array[43], 'Se cargan cuarenta y tres permisos RBAC base');
select results_eq(
    $$select count(*)::integer from permiso where codigo_permiso = 'CATALOGO_SEXO_LEER'$$,
    array[1],
    'Existe permiso granular de lectura para catalogo sexo'
);
select results_eq(
    $$select count(*)::integer from permiso where codigo_permiso = 'PARTICIPANTE_CREAR'$$,
    array[1],
    'Existe permiso granular de creacion de participantes'
);
select results_eq(
    $$select count(*)::integer from permiso where codigo_permiso = 'ROL_MODIFICAR'$$,
    array[1],
    'Existe permiso granular para modificar roles'
);
select results_eq(
    $$select count(*)::integer from permiso where codigo_permiso in ('USUARIO_GESTIONAR', 'ROL_PERMISO_GESTIONAR', 'PARTICIPANTE_GESTIONAR', 'RESPUESTA_REGISTRAR')$$,
    array[0],
    'No se cargan permisos agregados u obsoletos'
);
select results_eq('select count(*)::integer from estrategia_calificacion', array[7], 'Se cargan siete estrategias base');

select * from finish();
rollback;
