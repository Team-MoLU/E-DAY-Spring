package team.molu.edayserver.controller;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.repository.JwtRepository;
import team.molu.edayserver.repository.UserRepository;
import team.molu.edayserver.security.oauth2.jwt.JwtUtil;

@Controller
@RequiredArgsConstructor
@Slf4j
@ResponseBody
public class LoginController {
    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;
    private final JwtRepository jwtRepository;

//    @GetMapping("/login")
//    public String loginPage() {
//
//        return "test";
//    }

    /** 로그인 */
    @PostMapping("login")
    public void login(HttpServletRequest request, HttpServletResponse response) {

    }

    /** reissue */
    @PostMapping("reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        if (refresh.isBlank()) {

            //response status code
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            return new ResponseEntity<>("refresh token expired", HttpStatus.BAD_REQUEST);
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //DB에 저장되어 있는지 확인
        Jwt jwt = jwtRepository.findJwtByEmail(jwtUtil.getEmail(refresh)).block();
        if (jwt == null) {
            //response body
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        String email = jwtUtil.getEmail(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", email, role, 60*60*1000L);
        String newRefresh = jwtUtil.createJwt("refresh", email, role, 24*60*60*1000L);

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        Jwt deleteJwt = jwtRepository.findJwtByEmail(email).block();
        jwtRepository.delete(deleteJwt);

        Jwt newJwt = Jwt.builder()
                .user(userRepository.findUserByEmail(email).block())
                .refresh(newRefresh)
                .ttl(jwtUtil.getTtl(newRefresh))
                .build();
        jwtRepository.save(newJwt);

        //response
        response.addCookie(createCookie("access", newAccess));
        response.addCookie(createCookie("refresh", newRefresh));

        return new ResponseEntity<>(HttpStatus.OK);
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

    /** OAuth2 test용 delete User */
    @DeleteMapping("oauth2/users/{email}")
    public void deleteUsers(@PathVariable String email) {
        Mono<Void> del = userRepository.deleteUser(email);
    }

    /** logout */
    @PostMapping("logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        //get refresh token
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals("refresh")) {

                refresh = cookie.getValue();
            }
        }

        //refresh null check
        if (refresh.isBlank()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {

            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            //DB에 저장되어 있는지 확인
            Jwt jwt = jwtRepository.findJwtByEmail(jwtUtil.getEmail(refresh)).block();

            //로그아웃 진행
            //Refresh 토큰 DB에서 제거
            jwtRepository.delete(jwt);

            //Refresh 토큰 Cookie 값 0
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");

            response.addCookie(cookie);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}

