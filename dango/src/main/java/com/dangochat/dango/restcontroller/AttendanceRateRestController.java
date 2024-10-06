package com.dangochat.dango.restcontroller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/member")
public class AttendanceRateRestController {

    private final StudyService studyService;

    @PostMapping("/dates")
    public String getUserStudyDates(@AuthenticationPrincipal AuthenticatedUser userDetails) {

        // 로그인 된 사용자의 학습 날짜 목록 가져오기
        List<String> studyDates = studyService.getUserStudyDates(userDetails.getId());

        // 이번 달의 첫 날부터 오늘까지의 날짜 계산 (10월 6일이면 = 6)
        int thisMonthCount = LocalDate.now().getDayOfMonth();

        // 이번 달 학습 날짜 수 세기
        int thisMonthStudyCount = (int) studyDates.stream()
                // 1. 유저가 공부한 날짜의 달 = 이번 달 같은 것만
                .filter(date -> LocalDate.parse(date).getMonth() == LocalDate.now().getMonth())
                // 2. 개수세기
                .count();

        // 출석률 계산 (오늘까지 날짜 중에서 공부한 날짜 수 비율, 퍼센트로 변환 후 반올림)
        int attendanceRate = (int) Math.round(((double) thisMonthStudyCount / thisMonthCount) * 100);

//        log.info("이번 달 학습 날짜 수: {}", thisMonthStudyCount);
//        log.info("이번 달 날짜 수 : {}", thisMonthCount);
//        log.info("목록: {}%", studyDates);
        log.info("출석률: {}%", attendanceRate);
        return "attendanceRate";
    }
}
