package com.dangochat.dango.controller;

import com.dangochat.dango.service.*;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.security.AuthenticatedUser;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * KorLevelupQuizController
 * URL 패턴을 quiz/levelup/kor/{questionNumber}로 변경
 */

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/quiz/levelup/kor") //[]
public class KorLevelupTestController {

    private final KorLevelupTestService KorLevelupTestService;
    private final StudyRepository studyRepository;
    private final StudyService studyService; // 사용자의 학습 내용을 가져오는 서비스
    private final MemberService memberService;

    // 첫 번째 문제는 항상 /levelup/kor/1 번문제부터 시작하는 메서드
    @GetMapping("/1")
    public String levelupquiz(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {

        int userId = userDetails.getId();
        String currentLevel = memberService.getUserCurrentLevel(userId);
        session.setAttribute("level", currentLevel);

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("korGeneratedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1);
        session.setAttribute("currentMessageType", 2);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 2개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 2, currentLevel);
        log.info("초기 2개의 문제 생성 완료.");

        // 첫 번째 문제를 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0);
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1);
            log.info("첫 번째 문제 표시: {}", currentQuestion);
        }

        return "QuizView/levelupKor"; // levelupKor이라는 이름의 뷰로 이동
    }

    // 문제 번호를 URL로 받아 해당 문제를 출력하는 메서드
    @GetMapping("/{questionNumber}")
    public String levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {

        List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인 (24번째 문제까지만 허용)
        if (questionNumber > 24 || questionNumber < 1) {
            return "redirect:/";
        }

        if (korGeneratedQuestions != null && questionNumber <= korGeneratedQuestions.size()) {
            String currentQuestion = korGeneratedQuestions.get(questionNumber - 1);
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);
            session.setAttribute("currentIndex", questionNumber);
            log.info("현재 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        String level = session.getAttribute("level").toString();
        log.info("user level: {}", level);

        // n번째 문제를 풀 때, n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 24) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            korGenerateNextQuestionInBackground(session, (Integer) session.getAttribute("currentMessageType"), questionNumber + 2, level);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/levelupKor";
    }

    // 다음 문제로 이동하는 로직
    @PostMapping("/next")
    public String nextQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");

        if (currentIndex != null && korGeneratedQuestions != null && currentIndex < korGeneratedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        if (currentIndex != null && currentIndex >= 24) {
            return "redirect:/";
        }

        return "redirect:/quiz/levelup/kor/" + (currentIndex + 1);
    }

    // 초기 문제 3개를 미리 로드하는 메서드
    private void loadInitialQuestions(HttpSession session, int startIndex, int count, String level) {
        List<String> contentList = studyRepository.findByLevelContainingRandom(level);

        // 배열 형식으로 단어들을 로그에 출력
        log.info("뽑힌 단어 = {}", contentList);

        List<String> korGeneratedQuestions = new ArrayList<>();

        try {
            int messageType = (int) session.getAttribute("currentMessageType");
            korGeneratedQuestions = KorLevelupTestService.korGenerateQuestions(contentList.subList(startIndex - 1, startIndex - 1 + count), messageType, count, level);
            session.setAttribute("korGeneratedQuestions", korGeneratedQuestions);
            log.info("초기 생성된 {}개의 문제: {}", count, korGeneratedQuestions);
        } catch (Exception e) {
            log.error("문제가 생성되지 않았습니다.", e);
        }
    }

    // 백그라운드에서 다음 문제를 미리 생성하는 메서드
    private void korGenerateNextQuestionInBackground(HttpSession session, int currentMessageType, int targetIndex, String level) {
        new Thread(() -> {
            try {
                List<String> contentList = studyRepository.findByLevelContainingRandom(level);

                if (targetIndex < 25) {
                    List<String> nextQuestion = KorLevelupTestService.korGenerateQuestions(contentList.subList(targetIndex - 1, targetIndex), 2, 1, level);
                    List<String> korGeneratedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                    if (korGeneratedQuestions != null) {
                        korGeneratedQuestions.addAll(nextQuestion);
                        log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                    }

                    session.setAttribute("korGeneratedQuestions", korGeneratedQuestions);
                }
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start();
    }
}
