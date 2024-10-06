package com.dangochat.dango.restcontroller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.KorLevelupTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/quiz/levelup/kor")
public class KorLevelupTestRestController {

    private final KorLevelupTestService korLevelupTestService;

    // 첫 번째 문제 요청 시 문제를 생성하는 메서드 (항상 문제 3개 유지)
    @PostMapping("/start")
    public Map<String, Object> startQuiz(@RequestBody Map<String, Object> payload) {
        String level = (String) payload.get("level");
        int userId = (Integer) payload.get("userId");
        int totalQuestions = (Integer) payload.get("totalQuestions");

        // 초기 3개의 문제를 생성
        log.info("초기 3개의 문제 생성 시작.");
        List<String> initialQuestions = generateQuestions(level, 3);
        log.info("초기 3개의 문제 생성 완료: {}", initialQuestions);

        return createResponse(initialQuestions);
    }

    // 사용자가 문제를 풀고 다음 문제를 요청하는 메서드
    @PostMapping("/next")
    public Map<String, Object> nextQuestion(@RequestBody Map<String, Object> payload) {
        log.debug("payload : {}", payload);
        List<String> korGeneratedQuestions = (List<String>) payload.get("korGeneratedQuestions");
        String level = (String) payload.get("level");

        Map<String, Object> response = new HashMap<>();
        if (korGeneratedQuestions.isEmpty()) {
            response.put("status", "completed");
            return response;
        }

        // 문제 리스트에서 첫 번째 문제를 제거하고 새로운 문제를 추가하여 3개 유지
        korGeneratedQuestions.remove(0);
        List<String> newQuestions = generateQuestions(level, 1);
        korGeneratedQuestions.addAll(newQuestions);

        return createResponse(korGeneratedQuestions);
    }

    // 문제를 생성하는 메서드 (문제 개수 유지)
    private List<String> generateQuestions(String level, int count) {
        List<String> contentList = korLevelupTestService.findByKorWord(level);
        log.info("단어 목록: {}", contentList);

        List<String> newQuestions = null;
        try {
            int toIndex = Math.min(count, contentList.size());
            if (toIndex > 0) {
                newQuestions = korLevelupTestService.korGenerateQuestions(
                        contentList.subList(0, toIndex), 1, count, level);
                log.info("{}개의 새로운 문제 생성: {}", newQuestions.size(), newQuestions);
            } else {
                log.warn("단어 목록에 문제가 있습니다. 생성할 수 있는 문제가 없습니다.");
            }
        } catch (IOException e) {
            log.error("문제 생성 중 오류 발생: ", e);
        }

        return newQuestions;
    }

    // 문제 데이터를 JSON 형태로 변환하는 메서드
    private Map<String, Object> createResponse(List<String> questions) {
        Map<String, Object> response = new HashMap<>();
        response.put("questions", questions);  // 모든 문제 리스트를 반환
        return response;
    }
}
