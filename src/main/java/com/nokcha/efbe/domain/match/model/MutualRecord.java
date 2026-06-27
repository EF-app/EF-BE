package com.nokcha.efbe.domain.match.model;

import com.nokcha.efbe.domain.match.entity.MatchResult;

public record MutualRecord(
        MatchResult matchResult,
        boolean created
) { }