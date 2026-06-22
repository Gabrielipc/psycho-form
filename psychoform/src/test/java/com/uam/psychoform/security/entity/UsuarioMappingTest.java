package com.uam.psychoform.security.entity;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class UsuarioMappingTest {

    @Test
    void mapsUsuarioToTheApprovedTableAndColumns() throws Exception {
        assertThat(Usuario.class.getAnnotation(Table.class).name()).isEqualTo("usuario");

        Field identifier = Usuario.class.getDeclaredField("id");
        Field passwordHash = Usuario.class.getDeclaredField("hashContrasena");

        assertThat(identifier.getAnnotation(Column.class).name()).isEqualTo("usuario_id");
        assertThat(passwordHash.getAnnotation(Column.class).name()).isEqualTo("hash_contrasena");
    }

    @Test
    void mapsTheRoleAndPermissionCatalogs() {
        assertThat(Rol.class.getAnnotation(Table.class).name()).isEqualTo("rol");
        assertThat(Permiso.class.getAnnotation(Table.class).name()).isEqualTo("permiso");
    }

    @Test
    void compositeIdentifiersImplementValueEquality() {
        assertThat(UsuarioRolId.class.getDeclaredMethods())
                .extracting(method -> method.getName())
                .contains("equals", "hashCode");
        assertThat(RolPermisoId.class.getDeclaredMethods())
                .extracting(method -> method.getName())
                .contains("equals", "hashCode");
    }
}
