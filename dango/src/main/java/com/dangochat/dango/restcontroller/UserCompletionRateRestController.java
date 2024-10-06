package com.dangochat.dango.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.UserCompletionRateEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.UserCompletionRateService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/CompletionRate")  // REST API에서는 보통 /api를 사용
public class UserCompletionRateRestController {

    private final UserCompletionRateService userCompletionRateService;
    private final MemberService memberService;

    @GetMapping("/CompletionRateData")
    public ResponseEntity<Map<String, Object>> getCompletionRateData(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        // 로그인한 유저의 ID 가져오기
        int userId = userDetails.getId();

        // 해당 유저의 completion rate 데이터 가져오기
        Optional<UserCompletionRateEntity> completionRateOpt = userCompletionRateService.getCompletionRatesByUserId(userId);

        Map<String, Object> response = new HashMap<>();

        // 데이터가 존재하는 경우
        if (completionRateOpt.isPresent()) {
            UserCompletionRateEntity latestCompletionRate = completionRateOpt.get();  // Optional에서 값 추출
            response.put("weeklyPoints", latestCompletionRate.getWeeklyPoints());
            response.put("totalPoints", latestCompletionRate.getTotalPoints());
        } else {
            response.put("weeklyPoints", 0);
            response.put("totalPoints", 0);
        }

        // 유저의 originalLevel 가져오기
        MemberEntity user = memberService.findById(userId);
        String originalLevel = user.getOriginalLevel();
        int levelNumber = 0;

        // originalLevel에 따른 숫자 설정
        switch (originalLevel) {
            case "N1":
                levelNumber = 36252;
                break;
            case "N2":
                levelNumber = 12320;
                break;
            case "N3":
                levelNumber = 8604;
                break;
            case "N4":
                levelNumber = 4385;
                break;
            case "N5":
                levelNumber = 1468;
                break;
            case "1":
                levelNumber = 1345;
                break;
            case "2":
                levelNumber = 2606;
                break;
            case "3":
                levelNumber = 18081;
                break;
            case "4":
                levelNumber = 24604;
                break;
            case "5":
                levelNumber = 45240;
                break;
            case "6":
                levelNumber = 87816;
                break;
            default:
                levelNumber = 0;  // 레벨이 없는 경우
        }

        // 퍼센티지 계산 (총 획득 포인트 / 현재 레벨 총 달성 포인트) * 100
        Integer totalPoints = (Integer) response.get("totalPoints");  // NullPointerException 방지
        int percentage = 0;
        if (totalPoints != null && levelNumber > 0) {
            percentage = (int) (((double) totalPoints / levelNumber) * 100);
        }

        // JSON 데이터에 추가
        response.put("levelNumber", levelNumber);
        response.put("percentage", percentage);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
