package team.molu.edayserver.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import team.molu.edayserver.service.UserService;

import java.util.Collection;
import java.util.Iterator;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainCotroller {

    private UserService userService;

    @GetMapping("/")
    public String mainAPI() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();

        return authentication.getName();
    }
}
