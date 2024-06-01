package team.molu.edayserver.controller;

import java.text.ParseException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import team.molu.edayserver.common.EdayCommon;
import team.molu.edayserver.domain.CommonTestDomain;
import team.molu.edayserver.dto.CommonTestDto;

@Controller
@Slf4j
public class CommonTestController2 {

	@GetMapping("/goSession")
    public ModelAndView goSession(HttpServletRequest request, Model model) throws ParseException {
    	
    	CommonTestDomain domain = new CommonTestDomain(123L,"b@naver.com","yeaji22");
    	
    	CommonTestDto dto = new CommonTestDto();
    	
    	dto = EdayCommon.dtoToDomain(domain, CommonTestDto.class);
    	
    	ModelAndView mov = new ModelAndView();
    	
		mov.addObject("user", domain);
		mov.setViewName("/sessionTest");
    	
    	return mov;
    }
}
