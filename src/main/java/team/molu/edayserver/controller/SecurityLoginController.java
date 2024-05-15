package team.molu.edayserver.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SecurityLoginController {
    @GetMapping("test2")
    public String home(HttpServletRequest request) {
        

        return "index";
    }
    
    @GetMapping("a")
    public Map<String,String> joinPage(Model model) {
		Map<String, String> map = new HashMap<>();
		
		map.put("apple", "1");
		map.put("banana", "2");
		
       return map;
    }

}
