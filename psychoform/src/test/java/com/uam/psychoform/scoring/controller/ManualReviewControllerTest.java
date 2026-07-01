package com.uam.psychoform.scoring.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.security.SecurityPermissions;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class ManualReviewControllerTest {
    @Test
    void endpointsUsanPermisosDeRevisionManual() throws Exception {
        Method pending = ManualReviewController.class.getMethod("pending");
        Method createPending = ManualReviewController.class.getMethod("createPending", Long.class);
        Method resolve = ManualReviewController.class.getMethod("resolve", Long.class, ManualReviewController.ReviewRequest.class);

        assertThat(pending.getAnnotation(PreAuthorize.class).value())
                .isEqualTo(SecurityPermissions.CALIFICACION_EJECUTAR + " or " + SecurityPermissions.RESULTADO_VER);
        assertThat(createPending.getAnnotation(PreAuthorize.class).value())
                .isEqualTo(SecurityPermissions.CALIFICACION_EJECUTAR);
        assertThat(resolve.getAnnotation(PreAuthorize.class).value())
                .isEqualTo(SecurityPermissions.CALIFICACION_EJECUTAR);
    }
}
