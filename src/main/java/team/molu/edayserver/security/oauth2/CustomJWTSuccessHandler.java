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
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.User;
import team.molu.edayserver.dto.CustomOAuth2User;
import team.molu.edayserver.repository.JwtRepository;
import team.molu.edayserver.repository.UserRepository;
import team.molu.edayserver.security.oauth2.jwt.JwtUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomJWTSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtRepository jwtRepository;
    private final UserRepository userRepository;
    private final AesUtil aesUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        log.info("customUserDetails : {}" , customUserDetails);

        String email = customUserDetails.getName();
        log.info("email : {}", email);

        User user = userRepository.findUserByEmail(email).block();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String accessToken = jwtUtil.createJwt("access", email, role, 60*60*1000L);     //1H
        String refreshToken = jwtUtil.createJwt("refresh", email, role, 7*24*60*60*1000L);    //7D (테스트 후 24H로 변경 예정)

        String encryptedJwt = null;
        try {
            encryptedJwt = aesUtil.aesCBCEncode(refreshToken);
            log.info("encryptedJwt : {}", encryptedJwt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Jwt refreshJwt = Jwt.builder()
                .refresh(encryptedJwt)
                .user(user)
                .ttl(jwtUtil.getTtl(refreshToken))
                .build();
//        user.updateJwt(refreshJwt);
        Jwt savedJwt = userRepository.findUserAndUpdateJwt(email, refreshJwt.getRefresh(), refreshJwt.getTtl()).block();

        log.info("access Expired Time : {}", jwtUtil.getTtl(accessToken));
        log.info("refresh Expired Time : {}", jwtUtil.getTtl(refreshToken));

        String redirectUrl = "http://localhost:3000/";
        response.addCookie(createCookie("access", accessToken));
        response.addCookie(createCookie("refresh", refreshToken));
        response.sendRedirect(redirectUrl);
    }

    /** 쿠키 생성 */
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60);
//        cookie.setSecure(true);  // HTTPS일 때 사용
        cookie.setPath("/");
//        cookie.setHttpOnly(true);
        log.info("Created Cookie: name={}, value={}", key, value);

        return cookie;
    }
}
