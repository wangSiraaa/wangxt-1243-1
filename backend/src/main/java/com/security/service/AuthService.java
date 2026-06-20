package com.security.service;

import com.security.common.LoginUser;
import com.security.common.exception.BusinessException;
import com.security.dto.LoginDTO;
import com.security.dto.TokenVO;
import com.security.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public TokenVO login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        if (authentication == null) {
            throw new BusinessException("用户名或密码错误");
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(loginUser);

        TokenVO tokenVO = new TokenVO();
        tokenVO.setToken(token);
        tokenVO.setUserId(loginUser.getId());
        tokenVO.setUsername(loginUser.getUsername());
        tokenVO.setRealName(loginUser.getRealName());
        tokenVO.setRole(loginUser.getRole());

        return tokenVO;
    }

    public TokenVO login(LoginDTO loginDTO) {
        return login(loginDTO.getUsername(), loginDTO.getPassword());
    }

    public LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException("用户未登录");
        }
        return (LoginUser) authentication.getPrincipal();
    }
}
