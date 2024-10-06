package com.dangochat.dango.restcontroller;

import com.dangochat.dango.service.*;
import com.dangochat.dango.security.AuthenticatedUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController ///@RestController
@RequestMapping("/api/quiz/levelup/kor")
public class KorLevelupTestRestController {

    private final KorLevelupTestService korLevelupTestService;
    private final MemberService memberService;

    // 첫 번째 문제는 항상 /levelup/kor/1 번 문제 부터 시작 하는 메서드
    @GetMapping("/1")
    public Map<String, Object> levelupquiz(HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();  // 로그인 한 사용자 아이디
        String currentLevel = memberService.getUserCurrentLevel(userId); //사용자의 현재 레벨
        session.setAttribute("level", currentLevel); //사용자의 현재 레벨을 세션에 저장

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("korGeneratedQuestions", new ArrayList<String>()); // 생성된 문제들을 저장할 리스트
        session.setAttribute("currentIndex", 1); //현재 문제의 인덱스는 1
     // session.setAttribute("currentMessageType", 2); // 메세지 유형은 2의 유형임

        // 초기 문제 3개를 미리 생성
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 3, currentLevel); //1번부터 총 3개의 문제를 만듬
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 JSON 으로 반환
        List<String> generatedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions"); //세션에 저장된 문제 리스트 가져오기
        Map<String, Object> response = new HashMap<>(); // json형식의 데이터를 담은 Map 생성
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 첫 번째 문제 가져오기
            response.put("question", currentQuestion); // 응답에 문제 추가
            response.put("currentIndex", 1); // 응답에 현재 문제의 인덱스 1 추가
            log.info("첫 번째 문제 표시: {}", currentQuestion);
        }

        return response;
    }

    // 문제 번호를 URL로 받아 해당 문제를 출력하는 메서드
    @GetMapping("/{questionNumber}")
    public Map<String, Object> levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions"); // 세션에 저장된 문제

        Map<String, Object> response = new HashMap<>();     // 응답 데이터를 담을 Map 생성
        if (questionNumber > 24 || questionNumber < 1) {    // 문제 번호가 1~24 범위 밖이면
            response.put("redirect", "/");                  // 잘못된 문제 번호일 경우 홈으로 리다이렉트
            return response;
        }

        if (korGeneratedQuestions != null && questionNumber <= korGeneratedQuestions.size()) {
            String currentQuestion = korGeneratedQuestions.get(questionNumber - 1); // 요청된 문제 번호에 해당하는 문제 가져오기
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);
            session.setAttribute("currentIndex", questionNumber);
            log.info("현재 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        String level = (String) session.getAttribute("level");
        log.info("user level: {}", level);

        // n번째 문제를 풀 때, n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 24) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            korGenerateNextQuestionInBackground(session, (Integer) session.getAttribute("currentMessageType"), questionNumber + 2, level);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return response;
    }

    // 다음 문제로 이동하는 로직
    @PostMapping("/next")
    public Map<String, Object> nextQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");

        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && korGeneratedQuestions != null && currentIndex < korGeneratedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        if (currentIndex != null && currentIndex >= 24) {
            response.put("redirect", "/");   // 24번 문제 이후 홈으로 리다이렉트
            return response;
        }

        response.put("redirect", "/api/quiz/levelup/kor/" + (currentIndex + 1));
        return response;
    }

    // 초기 문제 3개를 미리 로드하는 메서드
    private void loadInitialQuestions(HttpSession session, int startIndex, int count, String level) {
        List<String> contentList = korLevelupTestService.findByKorWord(level);      // 단어 목록 가져옴

        log.info("뽑힌 단어 = {}", contentList);

        try {
            int messageType = (int) session.getAttribute("currentMessageType");
            List<String> korGeneratedQuestions = korLevelupTestService.korGenerateQuestions(contentList.subList(startIndex - 1, Math.min(startIndex - 1 + count, contentList.size())), messageType, count, level);
            session.setAttribute("korGeneratedQuestions", korGeneratedQuestions);
            session.setAttribute("contentList", contentList);
            log.info("초기 생성된 {}개의 문제: {}", count, korGeneratedQuestions);
        } catch (Exception e) {
            log.error("문제가 생성되지 않았습니다.", e);
        }
    }

    // 백그라운드에서 다음 문제를 미리 생성하는 메서드
    private void korGenerateNextQuestionInBackground(HttpSession session, int currentMessageType, int targetIndex, String level) {
        new Thread(() -> {
            try {
                // 세션에서 contentList를 가져옴
                List<String> contentList = (List<String>) session.getAttribute("contentList");

                if (contentList == null) {
                    log.error("contentList가 세션에 없습니다.");
                    return;
                }

                // 남은 문제의 index 범위를 확인
                if (targetIndex < 25 && targetIndex - 1 < contentList.size()) {
                    // 다음 문제를 생성할 때 사용되지 않은 단어를 하나씩 사용
                    List<String> nextQuestion = korLevelupTestService.korGenerateQuestions(
                            contentList.subList(targetIndex - 1, targetIndex), // 사용되지 않은 단어를 사용
                            currentMessageType,
                            1,
                            level
                    );

                    // 세션에서 이미 생성된 문제 리스트를 가져옴
                    List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");

                    if (korGeneratedQuestions != null) {
                        // 새로운 문제를 기존 문제 리스트에 추가
                        korGeneratedQuestions.addAll(nextQuestion);
                        log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));

                        // 세션에 다시 저장
                        session.setAttribute("korGeneratedQuestions", korGeneratedQuestions);
                    }
                }
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start();
    }
}
