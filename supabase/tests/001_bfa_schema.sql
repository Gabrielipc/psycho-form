begin;

select plan(6);

select has_table('public', 'usuario', 'La tabla usuario existe');
select has_type('public', 'estado_general', 'El enum estado_general existe');
select has_view('public', 'detalle_resultado', 'La vista detalle_resultado existe');
select results_eq('select count(*)::integer from rol', array[4], 'Se cargan cuatro roles base');
select results_eq('select count(*)::integer from permiso', array[13], 'Se cargan trece permisos base');
select results_eq('select count(*)::integer from estrategia_calificacion', array[7], 'Se cargan siete estrategias base');

select * from finish();
rollback;
