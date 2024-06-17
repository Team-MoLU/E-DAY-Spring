package team.molu.edayserver.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import team.molu.edayserver.dto.CustomOAuth2User;

public class SecurityUtils {
    public static String getAuthenticatedUserEmail() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() instanceof CustomOAuth2User customOAuth2User) {
//            return customOAuth2User.getName();
//        }
//        return null;
        return "test@example.com";
    }
}
