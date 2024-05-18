package team.molu.edayserver.controller;

import java.text.ParseException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import team.molu.edayserver.common.EdayCommon;

@RestController
public class CommonTestController {

    @GetMapping("/commontest")
    public int TestApi() throws ParseException {
    	int result = EdayCommon.getDateInterval("2024-05-02","20240505");
    	
        return result;
    }
    
    @GetMapping("/commontest2")
    public String TestApi2() throws ParseException {
    	String result = EdayCommon.getFormatDate("20240603","yyyy년 MM월 dd일");
    	
        return result;
    }
    
    @GetMapping("/commontest3")
    public String TestApi3() throws ParseException {
    	String a = "null";
    	
    	if(EdayCommon.isEmpty(a)) {
    		a = "빈값";
    	}else {
    		a = "성공";
    	}
    	
        return a;
    }
}
