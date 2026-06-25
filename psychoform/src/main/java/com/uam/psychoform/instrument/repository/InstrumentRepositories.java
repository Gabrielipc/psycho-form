package com.uam.psychoform.instrument.repository;

import com.uam.psychoform.instrument.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositories del agregado de instrumento; sin exponer reglas al participante. */
interface TestPsicologicoRepository extends JpaRepository<TestPsicologico, Long> { }
interface SubtestRepository extends JpaRepository<Subtest, Long> { }
interface DimensionResultadoRepository extends JpaRepository<DimensionResultado, Long> { }
interface ItemRepository extends JpaRepository<Item, Long> { }
interface OpcionItemRepository extends JpaRepository<OpcionItem, Long> { }
interface RecursoMultimediaRepository extends JpaRepository<RecursoMultimedia, Long> { }
interface EstrategiaCalificacionRepository extends JpaRepository<EstrategiaCalificacion, Short> { }
interface ReglaCalificacionRepository extends JpaRepository<ReglaCalificacion, Long> { }
interface ClaveRespuestaRepository extends JpaRepository<ClaveRespuesta, Long> { }
