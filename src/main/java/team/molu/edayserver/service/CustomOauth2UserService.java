package team.molu.edayserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import team.molu.edayserver.domain.*;
import team.molu.edayserver.dto.CustomOAuth2User;
import team.molu.edayserver.dto.GoogleResponse;
import team.molu.edayserver.dto.OAuth2Response;
import team.molu.edayserver.dto.UserDto;
import team.molu.edayserver.repository.OauthRepository;
import team.molu.edayserver.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OauthRepository oauthRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("oAuth2User : {}", oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if(registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        //유저 정보 가지고 오기
        //만약 없다면 새로 등록하기
        User existData = userRepository.findUserByEmail(oAuth2Response.getEmail()).block();
        if(existData == null) {
            existData = userNotFoundCreateUser(oAuth2Response);
        }
        UserDto existDataDto = UserDto.builder()
                .email(existData.getEmail())
                .profileImage(existData.getProfileImage())
                .role(RoleEnum.MEMBER.toString())
                .build();

        return new CustomOAuth2User(existDataDto);

    }

    public User userNotFoundCreateUser(OAuth2Response oAuth2Response) {
        log.info("new User!");
        Task task = Task.builder()
//                .id("root")
//                .name("root")
                .build();

        Oauth createOauth = Oauth.builder()
                .oauthId(oAuth2Response.getEmail())
                .provider(OauthProviderEnum.GOOGLE)
                .build();

        Role createRole = Role.builder()
                .type(RoleEnum.MEMBER)
                .build();

        Jwt jwt = Jwt.builder()
                .build();

        User createUser = User.builder()
                .email(oAuth2Response.getEmail())
                .profileImage(oAuth2Response.getPicture())
                .userOauth(createOauth)
                .userRole(createRole)
                .rootTask(task)
                .userJwt(jwt)
                .build();

        User savedUser = userRepository.createUserAndAll(
                        createUser.getEmail(), createUser.getProfileImage(),
                        createOauth.getOauthId(), createOauth.getProvider(),
                        createRole.getType(), jwt.getRefresh(), jwt.getTtl())
                .block();

        return savedUser;
    }

}
