package com.nokcha.efbe.common.auth.model;

public record AuthAdminPrincipal(
        Long adminId,
        String loginId,
        String name,
        String role
) {
}
