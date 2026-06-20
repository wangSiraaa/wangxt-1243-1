package com.security.controller;

import com.security.common.LoginUser;
import com.security.common.Result;
import com.security.dto.LoginDTO;
import com.security.dto.TokenVO;
import com.security.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Result<TokenVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        TokenVO tokenVO = authService.login(loginDTO);
        return Result.success(tokenVO);
    }

    @GetMapping("/current-user")
    @PreAuthorize("isAuthenticated()")
    public Result<LoginUser> getCurrentUser() {
        LoginUser currentUser = authService.getCurrentUser();
        return Result.success(currentUser);
    }
}
