package com.dangochat.dango.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMileageDTO {

    private int userMileageId;                      // 마일리지 고유 아이디
    private int userId;                             // 회원 고유 아이디 (DTO에서는 ID만 전달)
    private int userMileageAmount;                  // 마일리지 양
    private LocalDateTime mileageLastUpdated;       // 마지막 업데이트 날짜

}