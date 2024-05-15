package team.molu.edayserver.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class testController {
	//@GetMapping("")
    public String start(HttpServletRequest request) {
        return "index";
    }
	
	@GetMapping("/test")
    public String test(HttpServletRequest request) {
        return "test.html";
    }
}
