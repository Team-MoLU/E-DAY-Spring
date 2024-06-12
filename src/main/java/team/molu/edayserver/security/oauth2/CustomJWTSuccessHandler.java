package team.molu.edayserver.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team.molu.edayserver.dto.CustomOAuth2User;
import team.molu.edayserver.security.oauth2.jwt.JwtUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJWTSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("onAuthenticationSuccess");
        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        log.info("customUserDetails : {}" ,customUserDetails);

        String email = customUserDetails.getName();
        log.info("email : {}" ,email);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(email, role, 60*60*60L);

        log.info("JWT Token to be added in Cookie: {}", token);

        String redirectUrl = "http://localhost:3000/login";
        response.addCookie(createCookie("Authorization", token));
//        response.sendRedirect(UriComponentsBuilder.fromUriString("http://localhost:3000/login")
//                .queryParam("Authorization")
//                .build()
//                .encode(StandardCharsets.UTF_8)
//                .toUriString());
        response.sendRedirect(redirectUrl);
    }

    /** 쿠키 생성 */
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
//        cookie.setSecure(true);  // HTTPS일 때 사용
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        log.info("Created Cookie: name={}, value={}", key, value);

        return cookie;
    }
}
