package com.security.dto;

import com.security.common.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenVO {

    private String token;
    private UUID userId;
    private String username;
    private String realName;
    private UserRole role;
}
