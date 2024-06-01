package team.molu.edayserver.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import team.molu.edayserver.domain.CommonTestDomain;

@Slf4j
public class UserInfo {
	
	private static CommonTestDomain user = (CommonTestDomain) getSession().getAttribute("user");
	
	private static String id = ((Long) getSession().getAttribute("id")).toString();;
	
	private static String name = (String) getSession().getAttribute("name");;
	
	private static String email = (String) getSession().getAttribute("email");;

	private static HttpSession getSession() {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession session = servletRequestAttribute.getRequest().getSession(true);
		
		return session;
	}
	
	public static CommonTestDomain getUserIfno() {
		return user;
	}
	
	public static String getUserId() {
		return id;
	}
	
	public static String getUsername() {
		return name;
	}
	
	public static String getUseremail() {
		return email;
	}
}
