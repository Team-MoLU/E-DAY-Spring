package team.molu.edayserver.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.OauthProviderEnum;
import team.molu.edayserver.domain.User;
import team.molu.edayserver.exception.UserNotFoundException;
import team.molu.edayserver.repository.JwtRepository;
import team.molu.edayserver.repository.OauthRepository;
import team.molu.edayserver.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OauthRepository oauthRepository;

    @Mock
    private JwtRepository jwtRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private Oauth oauth;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("id")
                .email("test@example.com")
                .profileImage("profile.jpg")
                .build();

        oauth = Oauth.builder()
                .oauthId("oauth123")
                .provider(OauthProviderEnum.GOOGLE)
                .user(user)
                .build();

        jwt = Jwt.builder()
                .refresh("refresh123")
                .ttl(LocalDateTime.now())
                .user(user)
                .build();
    }

    @Test
    void findUserByEmail_shouldReturnUser_whenUserExists() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.just(user));

        User foundUser = userService.findUserByEmail(user.getEmail());

        assertEquals(user, foundUser);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
    }

    @Test
    void findUserByEmail_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserByEmail(user.getEmail()));
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
    }

    @Test
    void createUser_shouldSaveUser() {
        when(userRepository.save(user)).thenReturn(Mono.just(user));

        userService.createUser(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .profileImage("updated.jpg")
                .build();

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updatedUser));

        boolean updated = userService.updateUser(updatedUser);

        assertTrue(updated);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_shouldReturnFalse_whenUserDoesNotExist() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean updated = userService.updateUser(user);

        assertFalse(updated);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserByEmail_shouldDeleteUser_whenUserExists() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(userRepository.delete(user)).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserByEmail(user.getEmail());

        assertTrue(deleted);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserByEmail_shouldReturnFalse_whenUserDoesNotExist() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserByEmail(user.getEmail());

        assertFalse(deleted);
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void createUserOauth_shouldSaveOauth_whenUserExists() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(oauthRepository.save(any(Oauth.class))).thenReturn(Mono.just(oauth));

        userService.createUserOauth(oauth, user.getEmail());

        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(oauthRepository, times(1)).save(any(Oauth.class));
    }

    @Test
    void createUserOauth_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.empty());

        assertThrows(UserNotFoundException.class, () -> userService.createUserOauth(oauth, user.getEmail()));
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(oauthRepository, never()).save(any(Oauth.class));
    }

    @Test
    void updateUserOauth_shouldUpdateOauth_whenOauthExists() {
        Oauth updatedOauth = Oauth.builder()
                .oauthId(oauth.getOauthId())
                .provider(OauthProviderEnum.NAVER)
                .user(user)
                .build();

        when(oauthRepository.findOauthByEmail(user.getEmail())).thenReturn(Mono.just(oauth));
        when(oauthRepository.save(any(Oauth.class))).thenReturn(Mono.just(updatedOauth));

        boolean updated = userService.updateUserOauth(updatedOauth, user.getEmail());

        assertTrue(updated);
        verify(oauthRepository, times(1)).findOauthByEmail(user.getEmail());
        verify(oauthRepository, times(1)).save(any(Oauth.class));
    }

    @Test
    void updateUserOauth_shouldReturnFalse_whenOauthDoesNotExist() {
        when(oauthRepository.findOauthByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean updated = userService.updateUserOauth(oauth, user.getEmail());

        assertFalse(updated);
        verify(oauthRepository, times(1)).findOauthByEmail(user.getEmail());
        verify(oauthRepository, never()).save(any(Oauth.class));
    }

    @Test
    void deleteUserOauthByEmail_shouldDeleteOauth_whenOauthExists() {
        when(oauthRepository.findOauthByEmail(user.getEmail())).thenReturn(Mono.just(oauth));
        when(oauthRepository.delete(oauth)).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserOauthByEmail(user.getEmail());

        assertTrue(deleted);
        verify(oauthRepository, times(1)).findOauthByEmail(user.getEmail());
        verify(oauthRepository, times(1)).delete(oauth);
    }

    @Test
    void deleteUserOauthByEmail_shouldReturnFalse_whenOauthDoesNotExist() {
        when(oauthRepository.findOauthByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserOauthByEmail(user.getEmail());

        assertFalse(deleted);
        verify(oauthRepository, times(1)).findOauthByEmail(user.getEmail());
        verify(oauthRepository, never()).delete(any(Oauth.class));
    }

    @Test
    void createUserJwt_shouldSaveJwt_whenUserExists() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(jwtRepository.save(any(Jwt.class))).thenReturn(Mono.just(jwt));

        userService.createUserJwt(jwt, user.getEmail());

        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(jwtRepository, times(1)).save(any(Jwt.class));
    }

    @Test
    void createUserJwt_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Mono.empty());

        assertThrows(UserNotFoundException.class, () -> userService.createUserJwt(jwt, user.getEmail()));
        verify(userRepository, times(1)).findUserByEmail(user.getEmail());
        verify(jwtRepository, never()).save(any(Jwt.class));
    }

    @Test
    void updateUserJwt_shouldUpdateJwt_whenJwtExists() {
        Jwt updatedJwt = Jwt.builder()
                .refresh(jwt.getRefresh())
                .ttl(LocalDateTime.of(2024, 6, 10, 12,0))
                .user(user)
                .build();

        when(jwtRepository.findJwtByEmail(user.getEmail())).thenReturn(Mono.just(jwt));
        when(jwtRepository.save(any(Jwt.class))).thenReturn(Mono.just(updatedJwt));

        boolean updated = userService.updateUserJwt(updatedJwt, user.getEmail());

        assertTrue(updated);
        verify(jwtRepository, times(1)).findJwtByEmail(user.getEmail());
        verify(jwtRepository, times(1)).save(any(Jwt.class));
    }

    @Test
    void updateUserJwt_shouldReturnFalse_whenJwtDoesNotExist() {
        when(jwtRepository.findJwtByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean updated = userService.updateUserJwt(jwt, user.getEmail());

        assertFalse(updated);
        verify(jwtRepository, times(1)).findJwtByEmail(user.getEmail());
        verify(jwtRepository, never()).save(any(Jwt.class));
    }

    @Test
    void deleteUserJwtByEmail_shouldDeleteJwt_whenJwtExists() {
        when(jwtRepository.findJwtByEmail(user.getEmail())).thenReturn(Mono.just(jwt));
        when(jwtRepository.delete(jwt)).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserJwtByEmail(user.getEmail());

        assertTrue(deleted);
        verify(jwtRepository, times(1)).findJwtByEmail(user.getEmail());
        verify(jwtRepository, times(1)).delete(jwt);
    }

    @Test
    void deleteUserJwtByEmail_shouldReturnFalse_whenJwtDoesNotExist() {
        when(jwtRepository.findJwtByEmail(user.getEmail())).thenReturn(Mono.empty());

        boolean deleted = userService.deleteUserJwtByEmail(user.getEmail());

        assertFalse(deleted);
        verify(jwtRepository, times(1)).findJwtByEmail(user.getEmail());
        verify(jwtRepository, never()).delete(any(Jwt.class));
    }
}