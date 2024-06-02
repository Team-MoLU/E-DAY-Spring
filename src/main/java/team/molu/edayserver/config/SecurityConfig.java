package team.molu.edayserver.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import team.molu.edayserver.security.oauth2.CustomJWTSuccessHandler;
import team.molu.edayserver.security.oauth2.jwt.JwtFilter;
import team.molu.edayserver.security.oauth2.jwt.JwtUtil;
import team.molu.edayserver.service.CustomOauth2UserService;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomOauth2UserService customOAuth2UserService;
    private final CustomJWTSuccessHandler customJWTSuccessHandler;
    private final JwtUtil jwtUtil;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //cors
        http
                .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(3600L);

                        configuration.setExposedHeaders(Collections.singletonList("Set-Cookie"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));

                        return configuration;
                    }
                }));


        //csrf disable
        http
                .csrf((auth) -> auth.disable());

        //Form 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //HTTP Basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());


        //JWTFilter 추가
        http
//                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        //oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                .loginPage("/login")
                .userInfoEndpoint((userInfoEndpointConfig) ->
                        userInfoEndpointConfig.userService(customOAuth2UserService))
                .successHandler(customJWTSuccessHandler));

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated());

        //세션 설정 : STATELESS
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
