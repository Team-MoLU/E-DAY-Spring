package team.molu.edayserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.User;
import team.molu.edayserver.exception.UserNotFoundException;
import team.molu.edayserver.repository.JwtRepository;
import team.molu.edayserver.repository.OauthRepository;
import team.molu.edayserver.repository.UserRepository;

@Service
@Qualifier("neo4j")
@Slf4j
public class UserServiceNeo4jImpl implements UserService {
    private final UserRepository userRepository;
    private final OauthRepository oauthRepository;
    private final JwtRepository jwtRepository;

    public UserServiceNeo4jImpl(UserRepository userRepository, OauthRepository oauthRepository, JwtRepository jwtRepository) {
        this.userRepository = userRepository;
        this.oauthRepository = oauthRepository;
        this.jwtRepository = jwtRepository;
    }

    @Override
    public User findUserByEmail(String email) {
        Mono<User> userMono = userRepository.findUserByEmail(email);
        return userMono.switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .block();
    }

    @Override
    public void createUser(User user) {
        userRepository.save(user).subscribe();
    }

    @Override
    public boolean updateUser(User user) {
        return Boolean.TRUE.equals(userRepository.findUserByEmail(user.getEmail())
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + user.getEmail())))
                .flatMap(existingUser -> {
                    User updatedUser = User.builder()
                            .id(existingUser.getId())
                            .email(existingUser.getEmail())
                            .profileImage(user.getProfileImage() != null ? user.getProfileImage() : existingUser.getProfileImage())
                            .userRole(existingUser.getUserRole())
                            .userOauth(existingUser.getUserOauth())
                            .userJwt(existingUser.getUserJwt())
                            .build();

                    log.info("test");

                    return userRepository.save(updatedUser);
                })
                .map(savedUser -> true)
                .onErrorReturn(false)
                .block());
    }

    @Override
    public boolean deleteUserByEmail(String email) {
        return Boolean.TRUE.equals(userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(existingUser -> userRepository.delete(existingUser)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }

    @Override
    public void createUserOauth(Oauth oauth, String email) {
        userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(user -> {
                    Oauth newOauth = Oauth.builder()
                            .oauthId(oauth.getOauthId())
                            .provider(oauth.getProvider())
                            .user(user)
                            .build();
                    return oauthRepository.save(newOauth);
                })
                .block();
    }

    @Override
    public boolean updateUserOauth(Oauth oauth, String email) {
        return Boolean.TRUE.equals(oauthRepository.findOauthByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Oauth not found with email: " + email)))
                .flatMap(existingOauth -> oauthRepository.save(oauth))
                .map(savedOauth -> true)
                .onErrorReturn(false)
                .block());
    }

    @Override
    public boolean deleteUserOauthByEmail(String email) {
        return Boolean.TRUE.equals(oauthRepository.findOauthByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Oauth not found with email: " + email)))
                .flatMap(existingOauth -> oauthRepository.delete(existingOauth)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }

    @Override
    public void createUserJwt(Jwt jwt, String email) {
        userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(user -> {
                    Jwt newJwt = Jwt.builder()
                            .refresh(jwt.getRefresh())
                            .ttl(jwt.getTtl())
                            .user(user)
                            .build();
                    return jwtRepository.save(newJwt);
                })
                .block();
    }

    @Override
    public boolean updateUserJwt(Jwt jwt, String email) {
        return Boolean.TRUE.equals(jwtRepository.findJwtByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Jwt not found with email: " + email)))
                .flatMap(existingJwt -> jwtRepository.save(jwt))
                .map(savedJwt -> true)
                .onErrorReturn(false)
                .block());
    }

    @Override
    public boolean deleteUserJwtByEmail(String email) {
        return Boolean.TRUE.equals(jwtRepository.findJwtByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Jwt not found with email: " + email)))
                .flatMap(existingJwt -> jwtRepository.delete(existingJwt)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }
}