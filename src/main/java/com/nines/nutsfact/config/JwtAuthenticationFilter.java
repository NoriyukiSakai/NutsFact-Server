package com.nines.nutsfact.config;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                Integer userId = Integer.parseInt(claims.getSubject());
                Integer businessAccountId = claims.get("businessAccountId", Integer.class);
                String email = claims.get("email", String.class);
                Integer role = claims.get("role", Integer.class);

                // ビジネスアカウントに所属していないユーザーは認証を拒否
                // （運営管理者 role=0 を除く）
                if (businessAccountId == null && (role == null || role != 0)) {
                    // businessAccountIdがない場合は認証しない（ログアウト状態として扱う）
                    filterChain.doFilter(request, response);
                    return;
                }

                String authority = switch (role != null ? role : 99) {
                    case 0 -> "ROLE_SYSTEM_ADMIN";       // 運営管理者
                    case 10 -> "ROLE_BUSINESS_OWNER";    // ビジネスオーナー
                    case 21 -> "ROLE_BUSINESS_USER";     // ビジネス利用者
                    default -> "ROLE_GUEST";
                };

                AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                        .userId(userId)
                        .businessAccountId(businessAccountId)
                        .email(email)
                        .role(role)
                        .build();

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority(authority))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
