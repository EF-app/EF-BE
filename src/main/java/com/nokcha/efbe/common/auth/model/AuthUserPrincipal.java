package com.nokcha.efbe.common.auth.model;

import lombok.Builder;
import lombok.Getter;

import java.security.Principal;

@Getter
@Builder
public class AuthUserPrincipal implements Principal {

    private Long userId;
    private String loginId;

    @Override
    public String getName() {
        return loginId;
    }
}
