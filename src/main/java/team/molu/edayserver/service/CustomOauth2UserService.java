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
import team.molu.edayserver.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

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

        //리소스 서버에서 발급 받은 정보로 사용자를 특정할 아이디값을 만듬
        String username = oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();

        try {
            //유저 정보 가지고 오기
            User existData = userService.findUserByEmail(oAuth2Response.getEmail());
        } catch (UserNotFoundException userNotFoundException) {

            Oauth createOauth = Oauth.builder()
                    .oauthId(oAuth2Response.getEmail())
                    .provider(OauthProviderEnum.GOOGLE)
                    .build();

            Role createRole = Role.builder()
                    .type(RoleEnum.MEMBER)
                    .build();

            User createUser = User.builder()
                    .email(oAuth2Response.getEmail())
                    .profileImage(oAuth2Response.getPicture())
                    .userOauth(createOauth)
                    .userRole(createRole)
                    .build();

            userService.createUser(createUser);
            userService.createUserOauth(createOauth, oAuth2Response.getEmail());

            UserDto createdUserDto = UserDto.builder()
                    .email(createUser.getEmail())
                    .profileImage(createUser.getProfileImage())
                    .role(RoleEnum.MEMBER.toString())
                    .build();

            return new CustomOAuth2User(createdUserDto);
        }

        // 존재하는 유저 받아오기
        User existUser = userService.findUserByEmail(oAuth2Response.getEmail());
        UserDto existUserDto = UserDto.builder()
                .email(existUser.getEmail())
                .profileImage(existUser.getProfileImage())
                .role(RoleEnum.MEMBER.toString())
                .build();

        return new CustomOAuth2User(existUserDto);

    }
}