package com.dangochat.dango.controller;

import com.dangochat.dango.service.GPTQuizService;
import com.dangochat.dango.repository.StudyRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/quiz")
public class GPTQuizController {

    private final GPTQuizService gptQuizService;
    private final StudyRepository studyRepository;

    // 첫 번째 문제는 항상 /level/1에서 시작
    @GetMapping("/level/1")
    public String levelupquiz(Model model, HttpSession session) {
        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 2);  // 첫 번째 문제에서 3개의 문제 생성 (1, 2, 3번째 문제)
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 문제 표시: {}", currentQuestion);
        }

        return "QuizView/levelup";
    }

    // 문제 번호를 URL로 받아서 해당 문제를 출력
    @GetMapping("/level/{questionNumber}")
    public String levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session) {
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인 (24번째 문제까지만 허용)
        if (questionNumber > 24 || questionNumber < 1) {
            return "redirect:/"; // 범위를 벗어나면 홈으로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 24) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground(session, (Integer) session.getAttribute("currentMessageType"), questionNumber + 2);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/levelup";
    }

    // 다음 문제로 이동하는 로직
    @PostMapping("/next")
    public String nextQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= 24) {
            return "redirect:/";
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/level/" + (currentIndex + 1);
    }

    private void loadInitialQuestions(HttpSession session, int startIndex, int count) {
        List<String> contentList = studyRepository.findRandomContent(); // 24개의 단어 가져오기
        List<String> generatedQuestions = new ArrayList<>();

        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환
            generatedQuestions = gptQuizService.generateQuestions(contentList.subList(startIndex - 1, startIndex - 1 + count), messageType, count); // 여러 문제 생성
            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("문제가 생성되지 않았습니다.", e);
        }
    }

    private void generateNextQuestionInBackground(HttpSession session, int messageType, int targetIndex) {
        new Thread(() -> {
            try {
                List<String> contentList = studyRepository.findRandomContent();
                List<String> nextQuestion = gptQuizService.generateQuestions(contentList.subList(targetIndex - 1, targetIndex), messageType, 1); // targetIndex번째 문제 생성
                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion);
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                }

                session.setAttribute("generatedQuestions", generatedQuestions);
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start();
    }
}
