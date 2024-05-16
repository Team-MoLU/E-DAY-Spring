package team.molu.edayserver.service;

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
public class UserService {
    private final UserRepository userRepository;
    private final OauthRepository oauthRepository;
    private final JwtRepository jwtRepository;

    public UserService(UserRepository userRepository, OauthRepository oauthRepository, JwtRepository jwtRepository) {
        this.userRepository = userRepository;
        this.oauthRepository = oauthRepository;
        this.jwtRepository = jwtRepository;
    }

    /**
     * 주어진 email에 해당하는 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자 email
     * @return 사용자 객체, 해당 ID의 사용자가 없으면 null 반환
     */
    public User findUserByEmail(String email) {
        Mono<User> userMono = userRepository.findUserByEmail(email);
        return userMono.switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .block();
    }

    /**
     * 사용자 정보를 저장합니다.
     *
     * @param user 저장할 사용자 객체
     */
    public void createUser(User user) {
        userRepository.save(user).subscribe();
    }

    /**
     * 사용자 정보를 업데이트합니다.
     *
     * @param user 수정할 사용자 객체
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
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
                    return userRepository.save(updatedUser);
                })
                .map(savedUser -> true)
                .onErrorReturn(false)
                .block());
    }

    /**
     * 사용자를 삭제합니다.
     *
     * @param email 삭제할 사용자 Email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    public boolean deleteUserByEmail(String email) {
        return Boolean.TRUE.equals(userRepository.findUserByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with email: " + email)))
                .flatMap(existingUser -> userRepository.delete(existingUser)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }

    /**
     * 사용자 OAuth를 저장합니다.
     *
     * @param oauth 추가할 사용자 관련 oauth
     * @param email 사용자 Email
     */
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

    /**
     * 사용자 OAuth를 수정합니다.
     *
     * @param oauth 수정할 사용자 관련 oauth
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
    public boolean updateUserOauth(Oauth oauth, String email) {
        return Boolean.TRUE.equals(oauthRepository.findOauthByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Oauth not found with email: " + email)))
                .flatMap(existingOauth -> oauthRepository.save(oauth))
                .map(savedOauth -> true)
                .onErrorReturn(false)
                .block());
    }

    /**
     * 사용자 OAuth를 삭제합니다.
     *
     * @param email 삭제할 사용자 email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    public boolean deleteUserOauthByEmail(String email) {
        return Boolean.TRUE.equals(oauthRepository.findOauthByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Oauth not found with email: " + email)))
                .flatMap(existingOauth -> oauthRepository.delete(existingOauth)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }

    /**
     * 사용자 JWT를 저장합니다.
     *
     * @param jwt 추가할 사용자 관련 jwt
     * @param email 사용자 Email
     */
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

    /**
     * 사용자 JWT를 수정합니다.
     *
     * @param jwt 수정할 사용자 관련 jwt
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
    public boolean updateUserJwt(Jwt jwt, String email) {
        return Boolean.TRUE.equals(jwtRepository.findJwtByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Jwt not found with email: " + email)))
                .flatMap(existingJwt -> jwtRepository.save(jwt))
                .map(savedJwt -> true)
                .onErrorReturn(false)
                .block());
    }

    /**
     * 사용자 JWT를 삭제합니다.
     *
     * @param email 삭제할 사용자 email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    public boolean deleteUserJwtByEmail(String email) {
        return Boolean.TRUE.equals(jwtRepository.findJwtByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Jwt not found with email: " + email)))
                .flatMap(existingJwt -> jwtRepository.delete(existingJwt)
                        .then(Mono.just(true)))
                .onErrorReturn(false)
                .block());
    }
}
