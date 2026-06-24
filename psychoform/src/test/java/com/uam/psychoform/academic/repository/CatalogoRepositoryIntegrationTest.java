package com.uam.psychoform.academic.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.uam.psychoform.academic.entity.Carrera;
import com.uam.psychoform.academic.entity.CatalogoSexo;
import com.uam.psychoform.academic.entity.Cohorte;
import com.uam.psychoform.academic.entity.GrupoAcademico;
import com.uam.psychoform.security.entity.EstadoGeneral;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CatalogoRepositoryIntegrationTest {

    @Autowired
    private CatalogoSexoRepository catalogoSexoRepository;

    @Autowired
    private CarreraRepository carreraRepository;

    @Autowired
    private CohorteRepository cohorteRepository;

    @Autowired
    private GrupoAcademicoRepository grupoAcademicoRepository;

    @Test
    void buscaCatalogoSexoActivoPorCodigoYExcluyeInactivos() {
        CatalogoSexo activo = new CatalogoSexo();
        activo.setCodigo("QASEXO");
        activo.setNombre("Sexo de prueba");
        activo.setEstado(EstadoGeneral.ACTIVO);
        catalogoSexoRepository.saveAndFlush(activo);

        CatalogoSexo inactivo = new CatalogoSexo();
        inactivo.setCodigo("QASEXOINACTIVO");
        inactivo.setNombre("Sexo inactivo");
        inactivo.setEstado(EstadoGeneral.INACTIVO);
        catalogoSexoRepository.saveAndFlush(inactivo);

        assertThat(catalogoSexoRepository.buscarActivos("qasexo"))
                .extracting(CatalogoSexo::getCodigo)
                .contains("QASEXO")
                .doesNotContain("QASEXOINACTIVO");
    }

    @Test
    void buscaCarreraActivaPorNombreSinDistinguirMayusculas() {
        Carrera activa = carrera("QACARRERA", "Ingeniería de Prueba", EstadoGeneral.ACTIVO);
        carreraRepository.saveAndFlush(activa);

        Carrera inactiva = carrera("QACARRERAINACTIVA", "Ingeniería Inactiva", EstadoGeneral.INACTIVO);
        carreraRepository.saveAndFlush(inactiva);

        assertThat(carreraRepository.buscarActivos("INGENIERÍA DE PRUEBA"))
                .extracting(Carrera::getCodigoCarrera)
                .contains("QACARRERA")
                .doesNotContain("QACARRERAINACTIVA");
    }

    @Test
    void buscaCohorteActivaPorCodigoYExcluyeInactivos() {
        Cohorte activa = cohorte("QACOHORTE", "Cohorte de prueba", EstadoGeneral.ACTIVO);
        cohorteRepository.saveAndFlush(activa);

        Cohorte inactiva = cohorte("QACOHORTEINACTIVA", "Cohorte inactiva", EstadoGeneral.INACTIVO);
        cohorteRepository.saveAndFlush(inactiva);

        assertThat(cohorteRepository.buscarActivos("qacohorte"))
                .extracting(Cohorte::getCodigoCohorte)
                .contains("QACOHORTE")
                .doesNotContain("QACOHORTEINACTIVA");
    }

    @Test
    void buscaGrupoAcademicoActivoPorNombreYExcluyeInactivos() {
        Carrera carrera = carrera("QAGRUPOS", "Carrera para grupos", EstadoGeneral.ACTIVO);
        carreraRepository.saveAndFlush(carrera);

        GrupoAcademico activo = grupo(carrera, "QAGRUPO", "Grupo de Prueba", EstadoGeneral.ACTIVO);
        grupoAcademicoRepository.saveAndFlush(activo);

        GrupoAcademico inactivo = grupo(carrera, "QAGRUPOINACTIVO", "Grupo Inactivo", EstadoGeneral.INACTIVO);
        grupoAcademicoRepository.saveAndFlush(inactivo);

        assertThat(grupoAcademicoRepository.buscarActivos("GRUPO DE PRUEBA"))
                .extracting(GrupoAcademico::getCodigoGrupo)
                .contains("QAGRUPO")
                .doesNotContain("QAGRUPOINACTIVO");
    }

    private Carrera carrera(String codigo, String nombre, EstadoGeneral estado) {
        Carrera carrera = new Carrera();
        carrera.setCodigoCarrera(codigo);
        carrera.setNombreCarrera(nombre);
        carrera.setEstado(estado);
        return carrera;
    }

    private Cohorte cohorte(String codigo, String nombre, EstadoGeneral estado) {
        Cohorte cohorte = new Cohorte();
        cohorte.setCodigoCohorte(codigo);
        cohorte.setNombreCohorte(nombre);
        cohorte.setEstado(estado);
        return cohorte;
    }

    private GrupoAcademico grupo(Carrera carrera, String codigo, String nombre, EstadoGeneral estado) {
        GrupoAcademico grupo = new GrupoAcademico();
        grupo.setCarrera(carrera);
        grupo.setCodigoGrupo(codigo);
        grupo.setNombreGrupo(nombre);
        grupo.setEstado(estado);
        return grupo;
    }
}
