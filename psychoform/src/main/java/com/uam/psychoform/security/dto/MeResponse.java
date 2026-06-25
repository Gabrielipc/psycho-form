package com.uam.psychoform.security.dto;

import java.util.Set;
import java.util.UUID;

public record MeResponse(UUID userId, String username, Set<String> permissions, Set<String> roles) {
}