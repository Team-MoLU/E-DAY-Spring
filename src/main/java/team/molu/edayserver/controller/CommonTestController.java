package team.molu.edayserver.controller;

import java.text.ParseException;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import team.molu.edayserver.common.EdayCommon;
import team.molu.edayserver.common.UserInfo;
import team.molu.edayserver.domain.CommonTestDomain;
import team.molu.edayserver.dto.CommonTestDto;

@RestController
@Slf4j
public class CommonTestController {

    @GetMapping("/commontest")
    public int TestApi() throws ParseException {
    	int result = EdayCommon.getDateIntervalDay("2024-03-02","20240505");
    	
        return result;
    }
    
    @GetMapping("/commontest2")
    public String TestApi2() throws ParseException {
    	String result = EdayCommon.getFormatDate("20240603","yyyy년 MM월 dd일");
    	
        return result;
    }
    
    @GetMapping("/commontest3")
    public String TestApi3() throws ParseException {
    	
    	if(EdayCommon.isEmpty(null)) {
    		return "빈값";
    	}else {
    		return "성공";
    	}
    }
    
    @GetMapping("/commontest4")
    public String TestApi4() throws ParseException {
    	
    	return EdayCommon.getDayWeek("20240518");
    }
    
    @GetMapping("/commontest5")
    public String TestApi5() throws ParseException {
    	
    	return EdayCommon.getMaskValue("000123-1234567",8,7);
    }
    
    @GetMapping("/commontest6")
    public String TestApi6() throws ParseException {
    	
    	return EdayCommon.getNumberFormat("1234567");
    }
    
    @GetMapping("/commontest7")
    public String TestApi7() throws ParseException {
    	
    	CommonTestDto dto = new CommonTestDto("a@naver.com","yezi");
    	
    	log.info("dto: " + CommonTestDto.class);
    	
    	CommonTestDomain domain = new CommonTestDomain();
    	
    	domain = EdayCommon.dtoToDomain(dto, CommonTestDomain.class);
    	
    	return "result7: "+domain.getEmail();
    }
    
    @GetMapping("/commontest8")
    public String TestApi8() throws ParseException {
    	
    	CommonTestDomain domain = new CommonTestDomain(123L,"b@naver.com","yezi");
    	
    	CommonTestDto dto = new CommonTestDto();
    	
    	dto = EdayCommon.dtoToDomain(domain, CommonTestDto.class);
    	
    	return "result8: "+dto.getEmail();
    }
    
    /**
     * 로그인 후 사용자 정보 세션에 저장 처리
     * @return
     * @throws ParseException
     */
    @GetMapping("/sessionTest")
    public String sessionTest(HttpServletRequest request, Model model) throws ParseException {
    	
    	CommonTestDomain user = (CommonTestDomain) request.getAttribute("user");
    	
    	HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("id", user.getId());
        session.setAttribute("name", user.getName());
        session.setAttribute("email", user.getEmail());

        return user.getEmail();
    }
    
    /**
     * 로그인 세션에서 유저 정보 가져오기
     * @return
     * @throws ParseException
     */
    @GetMapping("/sessionGetTest")
    public String sessionGetTest(HttpServletRequest request, Model model) throws ParseException {

        return UserInfo.getUsername();
    }
}
