package team.molu.edayserver.controller;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.molu.edayserver.domain.Member;
import team.molu.edayserver.dto.JoinRequest;
import team.molu.edayserver.dto.LoginRequest;
import team.molu.edayserver.service.MemberService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginController {
	private final MemberService memberService;

    @GetMapping(value = {"", "/"})
    public String home(Model model) {
        
        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티 로그인");

        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iter = authorities.iterator();
        GrantedAuthority auth = iter.next();
        String role = auth.getAuthority();

        Member loginMember = memberService.getLoginMemberByLoginId(loginId);
        log.info("home");
        if (loginMember != null) {
            model.addAttribute("name", loginMember.getName());
        }

        return "home";
    }

    @GetMapping("/join")
    public String joinPage(Model model) {

        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티 로그인");
        log.info("join:get");
        // 회원가입을 위해서 model 통해서 joinRequest 전달
        model.addAttribute("joinRequest", new JoinRequest());
        return "join";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest,
                       BindingResult bindingResult, Model model) {

        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티 로그인");

        // 비밀번호 암호화 추가한 회원가입 로직으로 회원가입
        memberService.securityJoin(joinRequest);
        log.info("join:post");
        // 회원가입 시 홈 화면으로 이동
        return "redirect:/security-login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {

        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티 로그인");

        model.addAttribute("loginRequest", new LoginRequest());
        log.info("login");
        return "login";
    }

    @GetMapping("/info")
    public String memberInfo(Authentication auth, Model model) {
    
    
        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티 로그인");

        Member loginMember = memberService.getLoginMemberByLoginId(auth.getName());
        log.info("info");
        model.addAttribute("member", loginMember);
        return "info";
    }
    
    @GetMapping("/admin")
    public String adminPage(Model model) {
    	log.info("admin");
        model.addAttribute("loginType", "security-login");
        model.addAttribute("pageName", "스프링 시큐리티  로그인");


        return "admin";
    }
}
