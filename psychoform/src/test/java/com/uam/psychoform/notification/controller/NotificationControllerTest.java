package com.uam.psychoform.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uam.psychoform.audit.model.Auditoria;
import com.uam.psychoform.audit.repository.AuditoriaRepository;
import com.uam.psychoform.security.SecurityPermissions;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class NotificationControllerTest {
    private final AuditoriaRepository auditoria = Mockito.mock(AuditoriaRepository.class);
    private final NotificationController controller = new NotificationController(auditoria);

    @Test
    void listRequiereUsuarioAutenticado() throws Exception {
        Method method = NotificationController.class.getMethod("list");

        assertThat(method.getAnnotation(PreAuthorize.class).value()).isEqualTo(SecurityPermissions.AUTHENTICATED);
    }

    @Test
    void listDevuelveEventosRecientesDeAuditoria() {
        Auditoria row = new Auditoria();
        row.setId(10L);
        row.setAccion("REVISION_MANUAL_GUARDADA");
        row.setEntidad("revision_manual_respuesta");
        row.setEntidadId("55");
        row.setCreadoEn(LocalDateTime.parse("2026-06-24T12:00:00"));
        when(auditoria.findAllByOrderByCreadoEnDesc()).thenReturn(List.of(row));

        @SuppressWarnings("unchecked")
        List<NotificationController.NotificationView> notifications =
                (List<NotificationController.NotificationView>) controller.list().data();

        assertThat(notifications).singleElement().satisfies(notification -> {
            assertThat(notification.id()).isEqualTo(10L);
            assertThat(notification.action()).isEqualTo("REVISION_MANUAL_GUARDADA");
            assertThat(notification.entity()).isEqualTo("revision_manual_respuesta");
        });
    }
}
