package com.uam.psychoform.academic.controller;

import com.uam.psychoform.academic.model.*;
import com.uam.psychoform.academic.service.*;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import com.uam.psychoform.security.SecurityPermissions;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos")
public class CatalogController {
    private final CatalogoSexoService sexos;
    private final CarreraService carreras;
    private final CohorteService cohortes;
    private final GrupoAcademicoService grupos;

    public CatalogController(CatalogoSexoService sexos, CarreraService carreras, CohorteService cohortes,
            GrupoAcademicoService grupos) {
        this.sexos = sexos;
        this.carreras = carreras;
        this.cohortes = cohortes;
        this.grupos = grupos;
    }

    @GetMapping("/{type}")
    @PreAuthorize("@catalogPermissionEvaluator.canRead(authentication, #type)")
    public ApiResponse<?> list(@PathVariable String type, @RequestParam(defaultValue = "") String q) {
        return ApiResponse.ok(EntityView.of(service(type).buscarActivos(q)));
    }

    @PostMapping("/sexos")
    @PreAuthorize(SecurityPermissions.CATALOGO_SEXO_CREAR)
    public ApiResponse<?> createSex(@RequestBody CatalogoSexo request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(sexos.guardar(request)));
    }

    @PostMapping("/carreras")
    @PreAuthorize(SecurityPermissions.CARRERA_CREAR)
    public ApiResponse<?> createCareer(@RequestBody Carrera request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(carreras.guardar(request)));
    }

    @PostMapping("/cohortes")
    @PreAuthorize(SecurityPermissions.COHORTE_CREAR)
    public ApiResponse<?> createCohort(@RequestBody Cohorte request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(cohortes.guardar(request)));
    }

    @PostMapping("/grupos-academicos")
    @PreAuthorize(SecurityPermissions.GRUPO_ACADEMICO_CREAR)
    public ApiResponse<?> createGroup(@RequestBody GrupoAcademico request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(grupos.guardar(request)));
    }

    @SuppressWarnings("unchecked")
    @DeleteMapping("/{type}/{id}")
    @PreAuthorize("@catalogPermissionEvaluator.canDelete(authentication, #type)")
    public ApiResponse<Void> delete(@PathVariable String type, @PathVariable Short id) {
        service(type).eliminarPorId(id);
        return ApiResponse.ok(null);
    }

    @SuppressWarnings({ "rawtypes" })
    private CatalogoService service(String type) {
        return switch (type) {
            case "sexos" -> sexos;
            case "carreras" -> carreras;
            case "cohortes" -> cohortes;
            case "grupos-academicos" -> grupos;
            default -> throw new EntityNotFoundException("Catalogo no encontrado: " + type);
        };
    }
}
