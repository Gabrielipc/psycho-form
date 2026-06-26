package com.uam.psychoform.academic.service;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("catalogPermissionEvaluator")
public class CatalogPermissionEvaluator {
    private static final Map<String, String> TYPE_TO_RESOURCE = Map.of(
            "sexos", "CATALOGO_SEXO",
            "carreras", "CARRERA",
            "cohortes", "COHORTE",
            "grupos-academicos", "GRUPO_ACADEMICO");

    public boolean canRead(Authentication authentication, String type) {
        return hasPermission(authentication, resourceForType(type), "LEER");
    }

    public boolean canDelete(Authentication authentication, String type) {
        return hasPermission(authentication, resourceForType(type), "ELIMINAR");
    }

    public boolean canRead(Authentication authentication, Object service) {
        return hasPermission(authentication, resourceForService(service), "LEER");
    }

    public boolean canCreate(Authentication authentication, Object service) {
        return hasPermission(authentication, resourceForService(service), "CREAR");
    }

    public boolean canDelete(Authentication authentication, Object service) {
        return hasPermission(authentication, resourceForService(service), "ELIMINAR");
    }

    private static boolean hasPermission(Authentication authentication, String resource, String action) {
        if (authentication == null) {
            return false;
        }
        String authority = "PERM_" + resource + "_" + action;
        return authentication.getAuthorities().stream().anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    private static String resourceForType(String type) {
        String resource = TYPE_TO_RESOURCE.get(type);
        if (resource == null) {
            return "CATALOGO_DESCONOCIDO";
        }
        return resource;
    }

    private static String resourceForService(Object service) {
        return switch (service.getClass().getSimpleName()) {
            case "CatalogoSexoService" -> "CATALOGO_SEXO";
            case "CarreraService" -> "CARRERA";
            case "CohorteService" -> "COHORTE";
            case "GrupoAcademicoService" -> "GRUPO_ACADEMICO";
            default -> "CATALOGO_DESCONOCIDO";
        };
    }
}
