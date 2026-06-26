package com.uam.psychoform.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.academic.controller.CatalogController;
import com.uam.psychoform.academic.controller.ParticipantController;
import com.uam.psychoform.academic.service.CatalogoService;
import com.uam.psychoform.academic.service.ParticipanteService;
import com.uam.psychoform.assessment.controller.ParticipantAccessController;
import com.uam.psychoform.assessment.controller.ParticipantRuntimeController;
import com.uam.psychoform.assessment.controller.SessionController;
import com.uam.psychoform.assessment.service.ParticipantRuntimeService;
import com.uam.psychoform.assessment.service.SesionAplicacionService;
import com.uam.psychoform.assessment.service.SessionManagementService;
import com.uam.psychoform.audit.controller.AuditController;
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.instrument.controller.InstrumentController;
import com.uam.psychoform.instrument.service.InstrumentAdminService;
import com.uam.psychoform.instrument.service.VersionTestService;
import com.uam.psychoform.reporting.controller.ReportController;
import com.uam.psychoform.reporting.controller.ResultController;
import com.uam.psychoform.reporting.service.ReportRegistryService;
import com.uam.psychoform.reporting.service.ResultQueryService;
import com.uam.psychoform.scoring.service.ClaveSimpleScoringService;
import com.uam.psychoform.security.controller.AuthController;
import com.uam.psychoform.security.controller.RoleController;
import com.uam.psychoform.security.controller.UserController;
import com.uam.psychoform.security.service.UserManagementService;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class SecurityArchitectureTest {

    private static final List<Class<?>> CONTROLLERS = List.of(
            CatalogController.class,
            ParticipantController.class,
            ParticipantAccessController.class,
            ParticipantRuntimeController.class,
            SessionController.class,
            AuditController.class,
            InstrumentController.class,
            ReportController.class,
            ResultController.class,
            AuthController.class,
            RoleController.class,
            UserController.class);

    private static final List<Class<?>> RBAC_SERVICES = List.of(
            CatalogoService.class,
            ParticipanteService.class,
            SessionManagementService.class,
            SesionAplicacionService.class,
            ParticipantRuntimeService.class,
            InstrumentAdminService.class,
            VersionTestService.class,
            AuditLogService.class,
            ClaveSimpleScoringService.class,
            ResultQueryService.class,
            ReportRegistryService.class,
            UserManagementService.class);

    @Test
    void internalControllerMappingsRequireExplicitPreAuthorize() {
        List<String> unsecured = CONTROLLERS.stream()
                .flatMap(controller -> List.of(controller.getDeclaredMethods()).stream()
                        .filter(SecurityArchitectureTest::isMappedEndpoint)
                        .filter(method -> !isPublicEndpoint(controller, method))
                        .filter(method -> method.getAnnotation(PreAuthorize.class) == null)
                        .map(method -> controller.getSimpleName() + "." + method.getName()))
                .toList();

        assertThat(unsecured).isEmpty();
    }

    @Test
    void controllersDoNotInjectRepositoriesDirectly() {
        List<String> repositoryFields = CONTROLLERS.stream()
                .flatMap(controller -> List.of(controller.getDeclaredFields()).stream()
                        .filter(field -> Repository.class.isAssignableFrom(field.getType()))
                        .map(field -> controller.getSimpleName() + "." + field.getName()))
                .toList();

        assertThat(repositoryFields).isEmpty();
    }

    @Test
    void internalServiceEntryPointsRequireExplicitPreAuthorize() {
        List<String> unsecured = RBAC_SERVICES.stream()
                .flatMap(service -> List.of(service.getDeclaredMethods()).stream()
                        .filter(method -> Modifier.isPublic(method.getModifiers()))
                        .filter(method -> !isParticipantRuntimeTokenScoped(service, method))
                        .filter(method -> method.getAnnotation(PreAuthorize.class) == null)
                        .map(method -> service.getSimpleName() + "." + method.getName()))
                .toList();

        assertThat(unsecured).isEmpty();
    }

    private static boolean isMappedEndpoint(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(PatchMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class)
                || method.isAnnotationPresent(RequestMapping.class);
    }

    private static boolean isPublicEndpoint(Class<?> controller, Method method) {
        return controller.equals(AuthController.class) && method.getName().equals("login")
                || controller.equals(ParticipantAccessController.class) && method.getName().equals("validate")
                || controller.equals(ParticipantRuntimeController.class);
    }

    private static boolean isParticipantRuntimeTokenScoped(Class<?> service, Method method) {
        return service.equals(ParticipantRuntimeService.class) && !method.getName().equals("assignParticipant");
    }
}
