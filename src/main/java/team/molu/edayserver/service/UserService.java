package team.molu.edayserver.service;

import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.User;

public interface UserService {
    /**
     * 주어진 email에 해당하는 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자 email
     * @return 사용자 객체, 해당 ID의 사용자가 없으면 null 반환
     */
    User findUserByEmail(String email);

    /**
     * 사용자 정보를 저장합니다.
     *
     * @param user 저장할 사용자 객체
     */
    void createUser(User user);

    /**
     * 사용자 정보를 업데이트합니다.
     *
     * @param user 수정할 사용자 객체
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
    boolean updateUser(User user);

    /**
     * 사용자를 삭제합니다.
     *
     * @param email 삭제할 사용자 Email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    boolean deleteUserByEmail(String email);

    /**
     * 사용자 OAuth를 저장합니다.
     *
     * @param oauth 추가할 사용자 관련 oauth
     * @param email 사용자 Email
     */
    void createUserOauth(Oauth oauth, String email);

    /**
     * 사용자 OAuth를 수정합니다.
     *
     * @param oauth 수정할 사용자 관련 oauth
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
    boolean updateUserOauth(Oauth oauth, String email);

    /**
     * 사용자 OAuth를 삭제합니다.
     *
     * @param email 삭제할 사용자 email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    boolean deleteUserOauthByEmail(String email);

    /**
     * 사용자 JWT를 저장합니다.
     *
     * @param jwt 추가할 사용자 관련 jwt
     * @param email 사용자 Email
     */
    void createUserJwt(Jwt jwt, String email);

    /**
     * 사용자 JWT를 수정합니다.
     *
     * @param jwt 수정할 사용자 관련 jwt
     * @return 정상적으로 수정되었다면 true, 아니라면 false 반환
     */
    boolean updateUserJwt(Jwt jwt, String email);

    /**
     * 사용자 JWT를 삭제합니다.
     *
     * @param email 삭제할 사용자 email
     * @return 정상적으로 삭제되었다면 true, 아니라면 false 반환
     */
    boolean deleteUserJwtByEmail(String email);
}
