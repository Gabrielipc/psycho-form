package com.uam.psychoform.security;

public final class SecurityPermissions {
    private SecurityPermissions() {
    }

    public static final String CATALOGO_SEXO_LEER = "hasAuthority('PERM_CATALOGO_SEXO_LEER')";
    public static final String AUTHENTICATED = "isAuthenticated()";
    public static final String CATALOGO_SEXO_CREAR = "hasAuthority('PERM_CATALOGO_SEXO_CREAR')";
    public static final String CATALOGO_SEXO_MODIFICAR = "hasAuthority('PERM_CATALOGO_SEXO_MODIFICAR')";
    public static final String CATALOGO_SEXO_ELIMINAR = "hasAuthority('PERM_CATALOGO_SEXO_ELIMINAR')";

    public static final String CARRERA_CREAR = "hasAuthority('PERM_CARRERA_CREAR')";
    public static final String COHORTE_CREAR = "hasAuthority('PERM_COHORTE_CREAR')";
    public static final String GRUPO_ACADEMICO_CREAR = "hasAuthority('PERM_GRUPO_ACADEMICO_CREAR')";

    public static final String PARTICIPANTE_LEER = "hasAuthority('PERM_PARTICIPANTE_LEER')";
    public static final String PARTICIPANTE_CREAR = "hasAuthority('PERM_PARTICIPANTE_CREAR')";
    public static final String PARTICIPANTE_ELIMINAR = "hasAuthority('PERM_PARTICIPANTE_ELIMINAR')";
    public static final String PARTICIPANTE_ACCESO_GESTIONAR = "hasAuthority('PERM_PARTICIPANTE_ACCESO_GESTIONAR')";

    public static final String USUARIO_LEER = "hasAuthority('PERM_USUARIO_LEER')";
    public static final String USUARIO_CREAR = "hasAuthority('PERM_USUARIO_CREAR')";
    public static final String USUARIO_MODIFICAR = "hasAuthority('PERM_USUARIO_MODIFICAR')";

    public static final String ROL_LEER = "hasAuthority('PERM_ROL_LEER')";
    public static final String ROL_MODIFICAR = "hasAuthority('PERM_ROL_MODIFICAR')";

    public static final String TEST_LEER = "hasAuthority('PERM_TEST_CREAR') or hasAuthority('PERM_TEST_PUBLICAR')";
    public static final String TEST_CREAR = "hasAuthority('PERM_TEST_CREAR')";
    public static final String TEST_PUBLICAR = "hasAuthority('PERM_TEST_PUBLICAR')";

    public static final String CALIFICACION_CONFIGURAR = "hasAuthority('PERM_CALIFICACION_CONFIGURAR')";
    public static final String CALIFICACION_EJECUTAR = "hasAuthority('PERM_CALIFICACION_EJECUTAR')";
    public static final String BAREMO_CONFIGURAR = "hasAuthority('PERM_BAREMO_CONFIGURAR')";
    public static final String SESION_LEER = "hasAuthority('PERM_SESION_APLICAR') or hasAuthority('PERM_SESION_CREAR')";
    public static final String SESION_CREAR = "hasAuthority('PERM_SESION_CREAR')";
    public static final String SESION_APLICAR = "hasAuthority('PERM_SESION_APLICAR')";
    public static final String RESULTADO_VER = "hasAuthority('PERM_RESULTADO_VER')";
    public static final String RESULTADO_AGREGADO_VER = "hasAuthority('PERM_RESULTADO_AGREGADO_VER') or hasAuthority('PERM_RESULTADO_VER')";
    public static final String REPORTE_EXPORTAR = "hasAuthority('PERM_REPORTE_EXPORTAR')";
    public static final String REPORTE_LEER = "hasAuthority('PERM_REPORTE_EXPORTAR') or hasAuthority('PERM_RESULTADO_VER')";
    public static final String AUDITORIA_VER = "hasAuthority('PERM_AUDITORIA_VER')";
    public static final String AUDITORIA_REGISTRAR = "hasAuthority('PERM_AUDITORIA_REGISTRAR')";
}
