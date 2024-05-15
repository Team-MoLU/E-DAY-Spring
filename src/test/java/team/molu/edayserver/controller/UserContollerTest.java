package team.molu.edayserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.OauthProviderEnum;
import team.molu.edayserver.domain.User;
import team.molu.edayserver.service.UserService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Oauth oauth;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
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
    void getUserByEmail_shouldReturnUser_whenUserExists() throws Exception {
        when(userService.findUserByEmail(user.getEmail())).thenReturn(user);

        mockMvc.perform(get("/api/v1/users")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk());

        verify(userService, times(1)).findUserByEmail(user.getEmail());
    }

    @Test
    void createUser_shouldReturnCreated() throws Exception {
        doNothing().when(userService).createUser(any(User.class));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void updateUser_shouldReturnOk_whenUserUpdated() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void updateUser_shouldReturnNotFound_whenUserNotUpdated() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(false);

        mockMvc.perform(put("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void deleteUser_shouldReturnNoContent_whenUserDeleted() throws Exception {
        when(userService.deleteUserByEmail(user.getEmail())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/users")
                        .param("email", user.getEmail()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserByEmail(user.getEmail());
    }

    @Test
    void deleteUser_shouldReturnNotFound_whenUserNotDeleted() throws Exception {
        when(userService.deleteUserByEmail(user.getEmail())).thenReturn(false);

        mockMvc.perform(delete("/api/v1/users")
                        .param("email", user.getEmail()))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUserByEmail(user.getEmail());
    }

    @Test
    void createUserOauth_shouldReturnCreated() throws Exception {
        doNothing().when(userService).createUserOauth(any(Oauth.class), eq(user.getEmail()));

        mockMvc.perform(post("/api/v1/users/oauth")
                        .param("email", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oauth)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUserOauth(any(Oauth.class), eq(user.getEmail()));
    }

    @Test
    void createUserJwt_shouldReturnCreated() throws Exception {
        doNothing().when(userService).createUserJwt(any(Jwt.class), eq(user.getEmail()));

        mockMvc.perform(post("/api/v1/users/jwt")
                        .param("email", user.getEmail())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwt)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUserJwt(any(Jwt.class), eq(user.getEmail()));
    }
}