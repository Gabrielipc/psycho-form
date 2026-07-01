package com.uam.psychoform.scoring.service;

import com.uam.psychoform.assessment.model.RespuestaItem;
import com.uam.psychoform.assessment.repository.RespuestaItemRepository;
import com.uam.psychoform.audit.service.AuditLogService;
import com.uam.psychoform.scoring.model.EstadoRevisionManual;
import com.uam.psychoform.scoring.model.RevisionManualRespuesta;
import com.uam.psychoform.scoring.repository.RevisionManualRespuestaRepository;
import com.uam.psychoform.security.CurrentActor;
import com.uam.psychoform.security.SecurityPermissions;
import com.uam.psychoform.security.model.Usuario;
import com.uam.psychoform.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ManualReviewService {
    private final RevisionManualRespuestaRepository reviews;
    private final RespuestaItemRepository answers;
    private final UsuarioRepository users;
    private final CurrentActor currentActor;
    private final AuditLogService audit;
    private final Clock clock;

    public ManualReviewService(RevisionManualRespuestaRepository reviews, RespuestaItemRepository answers,
            UsuarioRepository users, CurrentActor currentActor, AuditLogService audit, Clock clock) {
        this.reviews = reviews;
        this.answers = answers;
        this.users = users;
        this.currentActor = currentActor;
        this.audit = audit;
        this.clock = clock;
    }

    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR + " or " + SecurityPermissions.RESULTADO_VER)
    public List<PendingReviewView> pending() {
        return reviews.findPendingWithAnswerContext(EstadoRevisionManual.PENDIENTE).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR)
    public PendingReviewView createPendingForAnswer(Long answerId) {
        RespuestaItem answer = answers.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException("Respuesta no encontrada: " + answerId));
        RevisionManualRespuesta review = new RevisionManualRespuesta();
        review.setRespuesta(answer);
        review.setEstado(EstadoRevisionManual.PENDIENTE);
        review.setCreadoEn(LocalDateTime.now(clock));
        reviews.save(review);
        answer.setRequiereRevisionManual(true);
        answers.save(answer);
        return toView(review);
    }

    @Transactional
    @PreAuthorize(SecurityPermissions.CALIFICACION_EJECUTAR)
    public PendingReviewView resolve(Long reviewId, ReviewCommand command) {
        RevisionManualRespuesta review = reviews.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Revision no encontrada: " + reviewId));
        review.setEstado(command.approved() ? EstadoRevisionManual.REVISADO : EstadoRevisionManual.OBSERVADO);
        review.setPuntajeAsignado(command.score());
        review.setComentario(command.comment());
        review.setRevisadoPor(currentUser());
        review.setRevisadoEn(LocalDateTime.now(clock));
        reviews.save(review);
        audit.recordTrusted(new AuditLogService.AuditEvent("REVISION_MANUAL_GUARDADA", "revision_manual_respuesta",
                String.valueOf(reviewId), null,
                "{\"score\":" + command.score() + ",\"approved\":" + command.approved() + "}", null, null));
        return toView(review);
    }

    private PendingReviewView toView(RevisionManualRespuesta review) {
        RespuestaItem answer = review.getRespuesta();
        return new PendingReviewView(review.getId(), answer.getId(), answer.getIntento().getId(),
                answer.getIntento().getAsignacion().getParticipante().getId().toString(),
                answer.getIntento().getAsignacion().getParticipante().getNombres() + " "
                        + answer.getIntento().getAsignacion().getParticipante().getApellidos(),
                answer.getItem().getId(), answer.getItem().getCodigoItem(), answer.getItem().getEnunciado(),
                answer.getRespuestaTextoAbierto(), answer.getRespuestaNumerica(), review.getEstado().name(),
                review.getPuntajeAsignado(), review.getComentario(), review.getCreadoEn(), review.getRevisadoEn());
    }

    private Usuario currentUser() {
        return users.findById(currentActor.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
    }

    public record ReviewCommand(BigDecimal score, String comment, boolean approved) {
    }

    public record PendingReviewView(Long reviewId, Long answerId, Long attemptId, String participantId,
            String participantName, Long itemId, String itemCode, String prompt, String textAnswer,
            BigDecimal numericAnswer, String status, BigDecimal score, String comment, LocalDateTime createdAt,
            LocalDateTime reviewedAt) {
    }
}
