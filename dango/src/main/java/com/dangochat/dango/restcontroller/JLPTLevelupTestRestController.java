package com.dangochat.dango.restcontroller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.JLPTLevelupTestService;
import com.dangochat.dango.service.MemberService;
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
@RestController
@RequestMapping("/api/quiz/levelup/jlpt")
public class JLPTLevelupTestRestController {

    private final JLPTLevelupTestService jlptLevelupTestService;
    private final MemberService memberService;

    // 첫 번째 문제는 항상 /levelup/jlpt/1 번 문제부터 시작하는 메서드
    @GetMapping("/1")
    public Map<String, Object> levelupquiz(HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        String currentLevel = memberService.getUserCurrentLevel(userId);
        session.setAttribute("level", currentLevel);

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("jlptGeneratedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1);
        session.setAttribute("currentMessageType", 2);

        // 초기 문제 3개를 미리 생성
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 3, currentLevel);
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 JSON으로 반환
        List<String> generatedQuestions = (List<String>) session.getAttribute("jlptGeneratedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0);
            response.put("question", currentQuestion);
            response.put("currentIndex", 1);
            log.info("첫 번째 문제 표시: {}", currentQuestion);
        }

        return response;
    }

    // 문제 번호를 URL로 받아 해당 문제를 출력하는 메서드
    @GetMapping("/{questionNumber}")
    public Map<String, Object> levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        List<String> jlptGeneratedQuestions = (List<String>) session.getAttribute("jlptGeneratedQuestions");

        Map<String, Object> response = new HashMap<>();
        if (questionNumber > 24 || questionNumber < 1) {
            response.put("redirect", "/"); // 잘못된 문제 번호일 경우 홈으로 리다이렉트
            return response;
        }

        if (jlptGeneratedQuestions != null && questionNumber <= jlptGeneratedQuestions.size()) {
            String currentQuestion = jlptGeneratedQuestions.get(questionNumber - 1);
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
            jlptGenerateNextQuestionInBackground(session, (Integer) session.getAttribute("currentMessageType"), questionNumber + 2, level);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return response;
    }

    // 다음 문제로 이동하는 로직
    @PostMapping("/next")
    public Map<String, Object> nextQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> jlptGeneratedQuestions = (List<String>) session.getAttribute("jlptGeneratedQuestions");

        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && jlptGeneratedQuestions != null && currentIndex < jlptGeneratedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        if (currentIndex != null && currentIndex >= 24) {
            response.put("redirect", "/"); // 24번 문제 이후 홈으로 리다이렉트
            return response;
        }

        response.put("redirect", "/api/quiz/levelup/jlpt/" + (currentIndex + 1));
        return response;
    }

    // 초기 문제 3개를 미리 로드하는 메서드
    private void loadInitialQuestions(HttpSession session, int startIndex, int count, String level) {
        List<String> contentList = jlptLevelupTestService.findByJLPTWord(level); // 3개의 단어 목록 가져옴

        log.info("뽑힌 단어 = {}", contentList);

        try {
            int messageType = (int) session.getAttribute("currentMessageType");
            List<String> jlptGeneratedQuestions = jlptLevelupTestService.jlptGenerateQuestions(contentList.subList(startIndex - 1, Math.min(startIndex - 1 + count, contentList.size())), messageType, count, level);
            session.setAttribute("jlptGeneratedQuestions",jlptGeneratedQuestions);
            session.setAttribute("contentList", contentList);
            log.info("초기 생성된 {}개의 문제: {}", count, jlptGeneratedQuestions);
        } catch (Exception e) {
            log.error("문제가 생성되지 않았습니다.", e);
        }
    }

    // 백그라운드에서 다음 문제를 미리 생성하는 메서드
    private void jlptGenerateNextQuestionInBackground(HttpSession session, int currentMessageType, int targetIndex, String level) {
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
                    List<String> nextQuestion = jlptLevelupTestService.jlptGenerateQuestions(
                            contentList.subList(targetIndex - 1, targetIndex), // 사용되지 않은 단어를 사용
                            currentMessageType,
                            1,
                            level
                    );

                    // 세션에서 이미 생성된 문제 리스트를 가져옴
                    List<String> jlptGeneratedQuestions = (List<String>) session.getAttribute("jlptGeneratedQuestions");

                    if (jlptGeneratedQuestions != null) {
                        // 새로운 문제를 기존 문제 리스트에 추가
                        jlptGeneratedQuestions.addAll(nextQuestion);
                        log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));

                        // 세션에 다시 저장
                        session.setAttribute("jlptGeneratedQuestions", jlptGeneratedQuestions);
                    }
                }
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start();
    }
}
