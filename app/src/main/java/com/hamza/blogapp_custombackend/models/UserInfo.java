package com.hamza.blogapp_custombackend.models;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfo {
    private final String username;
    private final Date issuedAt;
    private final Date expiresAt;
}
