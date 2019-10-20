package com.ketteridge.mir.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple representation of the authenticated user.
 */
@Getter
@AllArgsConstructor
public class Authorization {
    private String auth;
}
