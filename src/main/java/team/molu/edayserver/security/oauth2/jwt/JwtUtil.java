package team.molu.edayserver.security.oauth2.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.repository.JwtRepository;
import team.molu.edayserver.repository.UserRepository;
import team.molu.edayserver.security.oauth2.AesUtil;
import team.molu.edayserver.service.CustomOauth2UserService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Log4j2
public class JwtUtil {

    private SecretKey secretKey;
    private JwtRepository jwtRepository;
    private AesUtil aesUtil;
    private UserRepository userRepository;

    public JwtUtil(@Value("${spring.jwt.secret}")String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("email", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Date getTtl(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public Boolean isExpired(String token) {
        boolean validation = false;
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date(System.currentTimeMillis()));
            validation = true;
        } catch (ExpiredJwtException e) {
            System.out.println(" Token expired ");
        } catch (SignatureException e) {
            log.info(CustomOauth2UserService.class.getName());
        } catch(Exception e){
            System.out.println(" Some other exception in JWT parsing ");
        }
        return validation;
    }

    public String createJwt(String category, String email, String role, Long expiredMs) {

         String token = Jwts.builder()
                .claim("category", category)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
        log.info("Generated Token: {}", token);
        return token;
    }

    public String getCategory(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    // accessToken 토큰 만료 시
    public void isExpiredAccessToken(String accessToken, String refresh, HttpServletResponse response) throws Exception {
        // 1. refreshToken을 userRepository에서 가져오기
        String email = getEmail(refresh);
        Jwt findJwt = jwtRepository.findJwtByEmail(email).block();
        String storedRefreshToken = aesUtil.aesCBCDecode(findJwt.getRefresh());

        // 2. refresh토큰이 만료 전인지 확인
        boolean refreshExpired = isExpired(storedRefreshToken);
        if (!refreshExpired) {
            // 3. refresh토큰의 이메일, 역할과 accessToken의 이메일, 역할 비교
            String refreshEmail = getEmail(storedRefreshToken);
            String refreshRole = getRole(storedRefreshToken);
            String accessEmail = getEmail(accessToken);
            String accessRole = getRole(accessToken);

            if (refreshEmail.equals(accessEmail) && refreshRole.equals(accessRole)) {
                // 4. 새로운 accessToken 발급
                String newAccessToken = createJwt("access", email, refreshRole, 60*1000L);
                String newRefresh = createJwt("refresh", email, refreshRole, 24*60*60*1000L);

                // AES 암호화
                newRefresh = aesUtil.aesCBCEncode(newRefresh);

                //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
                jwtRepository.delete(findJwt);

                Jwt newJwt = Jwt.builder()
                        .user(userRepository.findUserByEmail(email).block())
                        .refresh(newRefresh)
                        .ttl(getTtl(newRefresh))
                        .build();
                jwtRepository.save(newJwt);

                //response
                response.addCookie(createCookie("access", newAccessToken));
                response.addCookie(createCookie("refresh", newRefresh));

                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            log.info("Refresh token expired");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
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
