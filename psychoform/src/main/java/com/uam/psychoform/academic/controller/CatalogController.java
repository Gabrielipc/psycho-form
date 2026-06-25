package com.uam.psychoform.academic.controller;

import com.uam.psychoform.academic.model.*;
import com.uam.psychoform.academic.service.*;
import com.uam.psychoform.security.model.EstadoGeneral;
import com.uam.psychoform.dto.ApiResponse;
import com.uam.psychoform.dto.EntityView;
import jakarta.persistence.EntityNotFoundException;
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
    public ApiResponse<?> list(@PathVariable String type, @RequestParam(defaultValue = "") String q) {
        return ApiResponse.ok(EntityView.of(service(type).buscarActivos(q)));
    }

    @PostMapping("/sexos")
    public ApiResponse<?> createSex(@RequestBody CatalogoSexo request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(sexos.guardar(request)));
    }

    @PostMapping("/carreras")
    public ApiResponse<?> createCareer(@RequestBody Carrera request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(carreras.guardar(request)));
    }

    @PostMapping("/cohortes")
    public ApiResponse<?> createCohort(@RequestBody Cohorte request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(cohortes.guardar(request)));
    }

    @PostMapping("/grupos-academicos")
    public ApiResponse<?> createGroup(@RequestBody GrupoAcademico request) {
        request.setEstado(EstadoGeneral.ACTIVO);
        return ApiResponse.ok(EntityView.of(grupos.guardar(request)));
    }

    @SuppressWarnings("unchecked")
    @DeleteMapping("/{type}/{id}")
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
