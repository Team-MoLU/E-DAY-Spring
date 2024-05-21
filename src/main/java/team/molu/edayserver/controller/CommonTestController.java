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
}
