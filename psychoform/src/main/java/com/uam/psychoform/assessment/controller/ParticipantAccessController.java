package com.uam.psychoform.assessment.controller;

import com.uam.psychoform.assessment.dto.*;

import com.uam.psychoform.assessment.service.ParticipantAccessService;
import com.uam.psychoform.assessment.service.ParticipantJwtService;
import com.uam.psychoform.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/acceso-participante")
public class ParticipantAccessController {
    private final ParticipantAccessService access;
    private final ParticipantJwtService participantJwt;

    public ParticipantAccessController(ParticipantAccessService access, ParticipantJwtService participantJwt) {
        this.access = access;
        this.participantJwt = participantJwt;
    }

    @PostMapping("/validar")
    public ApiResponse<ParticipantAccessResponse> validate(@Valid @RequestBody ParticipantAccessRequest request) {
        var granted = access.grantParticipantAccess(request.assignmentId(), request.token());
        String token = participantJwt.issue(granted.participantId(), granted.assignmentId());
        return ApiResponse.ok(new ParticipantAccessResponse(granted.assignmentId(), granted.participantId(), token,
                "Bearer"));
    }
}


