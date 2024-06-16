package team.molu.edayserver.security.oauth2.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import team.molu.edayserver.dto.CustomOAuth2User;
import team.molu.edayserver.dto.UserDto;

import java.io.IOException;
import java.io.PrintWriter;

@Log4j2
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰이 없는 사용자가 무한 재로딩 오류 걸리지 않게 설정
        String requestUri = request.getRequestURI();

        if(requestUri.matches("^\\/login(?:\\/.*)?$")) {

            filterChain.doFilter(request, response);
            return;
        }
        if(requestUri.matches("^\\/oauth2(?:\\/.*)?$")) {

            filterChain.doFilter(request, response);
            return;
        }

        //cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        String accessToken = null;
        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            log.info("cookie name : {}", cookie.getName());
            if (cookie.getName().equals("access")) {
                accessToken = cookie.getValue();
                log.info("getCookie accessToken : {}", accessToken);
            } else if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
                log.info("getCookie refreshToken : {}", refreshToken);
            }
        }

        //Authorization 헤더 검증
        if (accessToken.isBlank()) {
            // 토큰 없으니 다음 필터로 넘기기
            filterChain.doFilter(request, response);
            return;
        }

        //토큰
        String token = accessToken;
        String refresh = refreshToken;

        try {
            boolean accessExpired = jwtUtil.isExpired(accessToken);
            log.info("{}", jwtUtil.getTtl(accessToken));
            if (accessExpired) {
                jwtUtil.isExpiredAccessToken(token, refresh, response);
            } else {
                return;
            }
        } catch (ExpiredJwtException e) {
            PrintWriter writer = response.getWriter();
            writer.println("Token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(token);

        if(!category.equals("access")) {
            PrintWriter writer = response.getWriter();
            writer.println("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //토큰에서 email과 role 획득
        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token);

        log.info("token email : {} \n token role : {}", email ,role);

        //userDTO를 생성하여 값 set
        UserDto userDto = UserDto.builder()
                .email(email)
                .role(role)
                .build();

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);
        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //스프링 시큐리티 컨텍스트에 저장
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
