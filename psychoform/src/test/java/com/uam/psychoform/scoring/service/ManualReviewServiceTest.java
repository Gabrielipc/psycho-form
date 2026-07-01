package com.uam.psychoform.scoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uam.psychoform.academic.model.Participante;
import com.uam.psychoform.assessment.model.AsignacionTest;
import com.uam.psychoform.assessment.model.IntentoTest;
import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.instrument.model.Item;
import com.uam.psychoform.scoring.model.EstadoRevisionManual;
import com.uam.psychoform.scoring.model.RevisionManualRespuesta;
import com.uam.psychoform.scoring.repository.RevisionManualRespuestaRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.prepost.PreAuthorize;

class ManualReviewServiceTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC);

    private final RevisionManualRespuestaRepository reviews = Mockito.mock(RevisionManualRespuestaRepository.class);
    private final RespuestaItemRepository answers = Mockito.mock(RespuestaItemRepository.class);
    private final UsuarioRepository users = Mockito.mock(UsuarioRepository.class);
    private final CurrentActor currentActor = Mockito.mock(CurrentActor.class);
    private final AuditLogService audit = Mockito.mock(AuditLogService.class);
    private final ManualReviewService service = new ManualReviewService(reviews, answers, users, currentActor, audit, CLOCK);

    @Test
    void metodosExponenPermisosCorrectos() throws Exception {
        Method pending = ManualReviewService.class.getMethod("pending");
        Method createPending = ManualReviewService.class.getMethod("createPendingForAnswer", Long.class);
        Method resolve = ManualReviewService.class.getMethod("resolve", Long.class, ManualReviewService.ReviewCommand.class);

        assertThat(pending.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('PERM_CALIFICACION_EJECUTAR') or hasAuthority('PERM_RESULTADO_VER')");
        assertThat(createPending.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('PERM_CALIFICACION_EJECUTAR')");
        assertThat(resolve.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('PERM_CALIFICACION_EJECUTAR')");
    }

    @Test
    void createPendingForAnswerMarcaRespuestaYCreaRevisionPendiente() {
        RespuestaItem answer = answerGraph(44L);
        when(answers.findById(44L)).thenReturn(Optional.of(answer));

        ManualReviewService.PendingReviewView view = service.createPendingForAnswer(44L);

        assertThat(answer.getRequiereRevisionManual()).isTrue();
        assertThat(view.answerId()).isEqualTo(44L);
        assertThat(view.status()).isEqualTo("PENDIENTE");
        verify(reviews).save(Mockito.argThat(review -> review.getRespuesta() == answer
                && review.getEstado() == EstadoRevisionManual.PENDIENTE
                && review.getCreadoEn().equals(CLOCK.instant().atZone(ZoneOffset.UTC).toLocalDateTime())));
        verify(answers).save(answer);
    }

    @Test
    void resolveAsignaPuntajeRevisorYAuditoria() {
        UUID reviewerId = UUID.randomUUID();
        Usuario reviewer = new Usuario();
        reviewer.setId(reviewerId);
        RevisionManualRespuesta review = new RevisionManualRespuesta();
        review.setId(55L);
        review.setRespuesta(answerGraph(44L));
        review.setEstado(EstadoRevisionManual.PENDIENTE);
        review.setCreadoEn(CLOCK.instant().atZone(ZoneOffset.UTC).toLocalDateTime());
        when(reviews.findById(55L)).thenReturn(Optional.of(review));
        when(currentActor.usuarioId()).thenReturn(reviewerId);
        when(users.findById(reviewerId)).thenReturn(Optional.of(reviewer));

        ManualReviewService.PendingReviewView view = service.resolve(55L,
                new ManualReviewService.ReviewCommand(new BigDecimal("7.50"), "Correcto", true));

        assertThat(view.status()).isEqualTo("REVISADO");
        assertThat(review.getPuntajeAsignado()).isEqualByComparingTo("7.50");
        assertThat(review.getRevisadoPor()).isSameAs(reviewer);
        verify(reviews).save(review);
        verify(audit).recordTrusted(Mockito.argThat(event -> event.action().equals("REVISION_MANUAL_GUARDADA")
                && event.entity().equals("revision_manual_respuesta")
                && event.entityId().equals("55")));
    }

    @Test
    void pendingDevuelveRevisionesPendientesOrdenadasComoRepositorio() {
        RevisionManualRespuesta review = new RevisionManualRespuesta();
        review.setId(55L);
        review.setRespuesta(answerGraph(44L));
        review.setEstado(EstadoRevisionManual.PENDIENTE);
        review.setCreadoEn(CLOCK.instant().atZone(ZoneOffset.UTC).toLocalDateTime());
        when(reviews.findByEstadoOrderByCreadoEnAsc(EstadoRevisionManual.PENDIENTE)).thenReturn(List.of(review));

        assertThat(service.pending()).singleElement().satisfies(view -> {
            assertThat(view.reviewId()).isEqualTo(55L);
            assertThat(view.answerId()).isEqualTo(44L);
            assertThat(view.participantName()).isEqualTo("Ana Lopez");
        });
    }

    private static RespuestaItem answerGraph(Long answerId) {
        Participante participant = new Participante();
        participant.setId(UUID.randomUUID());
        participant.setNombres("Ana");
        participant.setApellidos("Lopez");
        AsignacionTest assignment = new AsignacionTest();
        assignment.setParticipante(participant);
        IntentoTest attempt = new IntentoTest();
        attempt.setId(33L);
        attempt.setAsignacion(assignment);
        Item item = new Item();
        item.setId(22L);
        item.setCodigoItem("ITEM-22");
        item.setEnunciado("Pregunta abierta");
        RespuestaItem answer = new RespuestaItem();
        answer.setId(answerId);
        answer.setIntento(attempt);
        answer.setItem(item);
        answer.setRespuestaTextoAbierto("Respuesta");
        answer.setRespuestaNumerica(new BigDecimal("5"));
        answer.setRequiereRevisionManual(false);
        return answer;
    }
}
