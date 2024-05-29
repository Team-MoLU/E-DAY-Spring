package team.molu.edayserver.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonTestDto {
	 private final String email;
	 private final String name;
	 
	 @Builder
     public CommonTestDto(String email, String name) {
		 this.email = email;
         this.name = name;
     }
	 
	 @Builder
     public CommonTestDto() {
		 this.email = "";
         this.name = "";
     }
}
