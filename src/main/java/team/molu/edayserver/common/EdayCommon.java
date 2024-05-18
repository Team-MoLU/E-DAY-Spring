package team.molu.edayserver.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdayCommon {
	public static String test() {
		return "test";
	}
	
	/*
	 * 년도:yyyy, 월:MM, 일:dd, 시:hh(0~12), HH(0~24), 분:mm, 초:ss
	 */
	public static String getTodaySDate(String format) {
		String todaySDate = "";		
		
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		
		todaySDate = now.format(formatter);
		
		return todaySDate;
	}
	
	public static String getTodaySDate() {
		String todaySDate = getTodaySDate("yyyyMMdd");
		
		return todaySDate;
	}
	
	public static String getFormatDate(String date, String changeFormatType) throws ParseException {
		String formatDate = "";
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("");
		
		LocalDateTime dateTime;
		
		if(date.contains(":")) {
			if(date.contains("년") || date.contains("월")) {
				dateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss");
			}else if(date.contains("-")) {
				dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			}else {
				dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
			}
			
			dateTime = LocalDateTime.parse(date, dateFormat);
		}else if(date.contains("시")) {
			if(date.contains("년") || date.contains("월")) {
				dateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초");
			}else if(date.contains("-")) {
				dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초");
			}else {
				dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH시 mm분 ss초");
			}
			
			dateTime = LocalDateTime.parse(date, dateFormat);
		}else if(date.contains("년") || date.contains("월")){
			dateFormat = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
			
			dateTime = LocalDate.parse(date, dateFormat).atStartOfDay();
		}else if(date.contains("-")) {
			dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			
			dateTime = LocalDate.parse(date, dateFormat).atStartOfDay();
		}else {
			dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
			
			dateTime = LocalDate.parse(date, dateFormat).atStartOfDay();
		}
        
        DateTimeFormatter changeFormat = DateTimeFormatter.ofPattern(changeFormatType);
        
        formatDate = dateTime.format(changeFormat);
		
		return formatDate;
	}
	
	public static int getDateInterval(String day1, String day2) throws ParseException {
		int dateInterval = 0;
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		LocalDateTime date1 = LocalDate.parse(getFormatDate(day1,"yyyyMMdd"), dateFormat).atStartOfDay();
		LocalDateTime date2 = LocalDate.parse(getFormatDate(day2,"yyyyMMdd"), dateFormat).atStartOfDay();
		
		Long diffDays = Duration.between(date1, date2).abs().toDays(); //일자수 차이
		dateInterval = diffDays.intValue();
		
		return dateInterval;
	}
	
	public static boolean isEmpty(String value) {
		boolean result = false;
		
		if(value == null) {
			result = true;
		}else if(value.trim().equals("")) {
			result = true;
		}else if(value.equals("null")) {
			result = true;
		}else if(value.equals("undefined")) {
			result = true;
		}
		
		return result;
	}
}
