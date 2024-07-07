package com.hamza.blogapp_custombackend.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class User {
    private String username;
    private String password;
    private String name;
    private List<Role> roles;
}
