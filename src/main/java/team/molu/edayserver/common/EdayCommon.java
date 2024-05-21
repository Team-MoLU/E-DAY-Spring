package team.molu.edayserver.common;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EdayCommon {
	public static String test() {
		return "test";
	}
	
	/*
	 * 오늘날짜 반환(포맷 지정)
	 * paramType: String
	 * param 설명: 년도:yyyy, 월:MM, 일:dd, 시:hh(0~12), HH(0~24), 분:mm, 초:ss
	 * resultType: String
	 */
	public static String getTodaySDate(String format) {
		String todaySDate = "";		
		
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
		
		todaySDate = now.format(formatter);
		
		return todaySDate;
	}
	
	/*
	 * 오늘날짜 반환(포맷 미지정)
	 * paramType: 없음
	 * resultType: String
	 */
	public static String getTodaySDate() {
		String todaySDate = getTodaySDate("yyyyMMdd");
		
		return todaySDate;
	}
	
	/*
	 * 날짜를 원하는 포맷으로 반환
	 * paramType: String, String
	 * param 설명: date-날짜, changeFormatType-원하는 포맷(hh는 사용불가, HH로 입력할 것)
	 * 지원하는 포맷: yyyy년 MM월 dd일, yyyy-MM-dd, yyyyMMdd, HH:mm:ss, HH시 mm분 ss초 (날짜,시분초 사이 띄어쓰기 필수 ex: yyyy년 MM월 dd일 HH시 mm분 ss초)
	 * resultType: String
	 */
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
		}else{
			dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
			
			dateTime = LocalDate.parse(date, dateFormat).atStartOfDay();
		}
        
        DateTimeFormatter changeFormat = DateTimeFormatter.ofPattern(changeFormatType);
        
        formatDate = dateTime.format(changeFormat);
		
		return formatDate;
	}
	
	/*
	 * 두 날짜간의 간력을 일수(day)로 반환
	 * paramType: String, String
	 * param 설명: day1-비교할 날짜1, day2-비교할 날짜
	 * resultType: String
	 */
	public static int getDateIntervalDay(String day1, String day2) throws ParseException {
		int dateInterval = 0;
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
		
		LocalDateTime date1 = LocalDate.parse(getFormatDate(day1,"yyyyMMdd"), dateFormat).atStartOfDay();
		LocalDateTime date2 = LocalDate.parse(getFormatDate(day2,"yyyyMMdd"), dateFormat).atStartOfDay();
		
		Long diffDays = Duration.between(date1, date2).abs().toDays(); //일자수 차이
		dateInterval = diffDays.intValue();
		
		return dateInterval;
	}
	
	/*
	 * 날짜의 요일을 반환
	 * paramType: String
	 * param 설명: date-요일을 반환하고 싶은 날짜
	 * resultType: String(월,화,수... 형태로만 반환 뒤에 요일이란 글자는 붙지 않음)
	 */
	public static String getDayWeek(String date) throws ParseException {
		String dayWeek = "";
		
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
		String formatDate = getFormatDate(date,"yyyyMMdd");
		DayOfWeek engDayWekk = LocalDate.parse(formatDate,dateFormat).getDayOfWeek();
		
		dayWeek = engDayWekk.getDisplayName(TextStyle.NARROW, Locale.KOREAN);
		
		return dayWeek;
	}
	
	/*
	 * 빈 값인지 확인
	 * paramType: String
	 * param 설명: value-확인할 값
	 * resultType: boolean-빈값일 경우 true를 반환
	 */
	public static boolean isEmpty(String value) {
		boolean result = false;
		
		if(Objects.isNull(value)) {
			result = true;
		}else if(value.trim().equals("")) {
			result = true;
		}else if(value.equals("null")) {
			result = true;
		}else if(value.equals("undefined")) {
			result = true;
		}else if(value.isEmpty()) {
			result = true;
		}
		
		return result;
	}
	
	/*
	 * 빈 값인지 확인
	 * paramType: Object
	 * param 설명: value-확인할 값
	 * resultType: boolean-빈값일 경우 true를 반환
	 */
	public static boolean isEmpty(Object value) {
		boolean result = false;
		
		if(Objects.isNull(value)) {
			result = true;
		}else if(value.equals("undefined")){
			result = true;
		}
		
		return result;
	}
	
	/*
	 * 마스킹 처리
	 * paramType: String
	 * param 설명: value-마스킹 처리할 값
	 * resultType: String
	 */
	public static String getMaskValue(String value, int start, int size) {
		String maskValue = "";
		
		maskValue = value.substring(0, start-1) + "*".repeat(size) + value.substring(start+size-1, value.length());
		
		return maskValue;
	}
	
	/*
	 * 숫자 세자리마다 , 표시
	 * paramType: String
	 * param 설명: number-
	 * resultType: String
	 */
	public static String getNumberFormat(String number) {
		String formatNumber = "";
		
		DecimalFormat df = new DecimalFormat("#,###");
		formatNumber = df.format(Long.parseLong(number));
		
		return formatNumber;
	}
}
