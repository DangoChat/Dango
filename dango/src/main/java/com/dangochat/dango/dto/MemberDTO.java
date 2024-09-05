package com.dangochat.dango.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {

	private int userId;
    private String userEmail;
    private String userPassword;
    private String nickname;
    private String userPhone;
    private String currentLevel;
    private int userMileage;
    private String userNationality;
    private String token;
}
