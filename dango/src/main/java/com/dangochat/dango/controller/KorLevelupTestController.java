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

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/quiz/levelup/kor")
public class KorLevelupTestController {

    private final KorLevelupTestService korLevelupTestService;
    private final StudyRepository studyRepository;
    private final StudyService studyService;
    private final MemberService memberService;

    // 첫 번째 문제는 항상 /levelup/kor/1 번 문제부터 시작하는 메서드
    @GetMapping("/1")
    public String levelupquiz(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        String currentLevel = memberService.getUserCurrentLevel(userId);
        session.setAttribute("level", currentLevel);

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("korGeneratedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1);
        session.setAttribute("currentMessageType", 2);

        // 초기 문제 3개를 미리 생성
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 3, currentLevel);
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0);
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1);
            log.info("첫 번째 문제 표시: {}", currentQuestion);
        }

        return "QuizView/levelupKor";
    }

    // 문제 번호를 URL로 받아 해당 문제를 출력하는 메서드
    @GetMapping("/{questionNumber}")
    public String levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        List<String> korGeneratedQuestions = (List<String>) session.getAttribute("korGeneratedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

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

        String level = (String) session.getAttribute("level");
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
        List<String> contentList = studyRepository.findByKorWordStart(level); // 3개의 단어 목록 가져옴

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