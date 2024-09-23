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

    // 첫 번째 문제는 항상 /level/ 1 번문제 부터 시작하는 메서드
    @GetMapping("/level/1")
    public String levelupquiz(Model model, HttpSession session) {
        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>()); // 생성된 문제들을 저장하는 리스트 초기화
        session.setAttribute("currentIndex", 1); // 현재 문제 번호 1로 설정
        session.setAttribute("currentMessageType", 1); // 현재 메세지 타입 1로 설정 (1번 프로미프트)

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialQuestions(session, 1, 2);  // 첫 번째 문제에서부터 3개의 문제를 생성하고 세션에 저장
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 첫 번째 문제를 가져옴
            model.addAttribute("question", currentQuestion); // 문제를 모델에 추가하여 뷰로 전달
            model.addAttribute("currentIndex", 1); // 현재 문제 번호를 모델에 추가하여 뷰로 전달
            log.info("첫 번째 문제 표시: {}", currentQuestion); // 문제 내용 로그 출력
        }

        return "QuizView/levelup"; // levelup이라는 이름의 뷰로 이동
    }

    // 문제 번호를 URL로 받아 해당 문제를 출력하는 메서드
    @GetMapping("/level/{questionNumber}")
    public String levelupquizWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session) {
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions"); // 세션에서 생성된 문제 리스트를 가져옴
        Integer currentIndex = (Integer) session.getAttribute("currentIndex"); // 세션에서 현재 문제 번호를 가져옴

        // 유효한 문제 번호인지 확인 (24번째 문제까지만 허용)
        if (questionNumber > 24 || questionNumber < 1) {
            return "redirect:/"; // 문제 번호가 범위를 벗어나면 홈으로 리다이렉트
        }

        // 문제 리스트가 존재하고, 해당 문제 번호에 맞는 문제가 있을 경우
        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 문제는 0-based index이므로 -1 해줌
            model.addAttribute("question", currentQuestion); // 해당 문제를 모델에 추가
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게 현재 문제 번호를 표시
            session.setAttribute("currentIndex", questionNumber); // 현재 문제 번호를 세션에 저장
            log.info("현재 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion); // 문제 번호와 문제 내용 로그 출력
        }

        // n번째 문제를 풀 때, n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 24) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground(session, (Integer) session.getAttribute("currentMessageType"), questionNumber + 2); // n+2번째 문제를 백그라운드에서 생성
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/levelup"; // levelup이라는 이름의 뷰로 이동
    }

    // 다음 문제로 이동하는 로직
    @PostMapping("/next")
    public String nextQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex"); // 세션에서 현재 문제 번호를 가져옴
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions"); // 세션에서 생성된 문제 리스트를 가져옴

        // 다음 문제로 인덱스를 증가시킴
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1); // 문제 번호를 1 증가시킴
            log.info("다음 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 경우 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= 24) {
            return "redirect:/"; // 마지막 문제 이후에는 홈으로 리다이렉트
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/level/" + (currentIndex + 1); // 다음 문제로 리다이렉트
    }

    // 초기 문제 3개를 미리 로드하는 메서드
    private void loadInitialQuestions(HttpSession session, int startIndex, int count) {
        List<String> contentList = studyRepository.findRandomContent(); // 24개의 단어를 데이터베이스에서 랜덤하게 가져옴
        List<String> generatedQuestions = new ArrayList<>();

        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 세션에서 현재 메세지 타입을 가져옴
            generatedQuestions = gptQuizService.generateQuestions(contentList.subList(startIndex - 1, startIndex - 1 + count), messageType, count); // 여러 문제를 생성하여 리스트에 저장
            session.setAttribute("generatedQuestions", generatedQuestions); // 생성된 문제 리스트를 세션에 저장
            log.info("초기 생성된 {}개의 문제: {}", count, generatedQuestions); //2개, 만둘아진 문제
        } catch (Exception e) {
            log.error("문제가 생성되지 않았습니다.", e); // 문제 생성 중 오류 발생 시 로그에 출력
        }
    }

    // 백그라운드에서 다음 문제를 미리 생성하는 메서드
    private void generateNextQuestionInBackground(HttpSession session, int currentMessageType, int targetIndex) {
        new Thread(() -> {
            try {
                List<String> contentList = studyRepository.findRandomContent(); // 24개의 단어를 랜덤하게 가져옴

                // targetIndex에 따라 적절한 messageType을 설정
                int messageType;
                if (targetIndex < 7) {
                    messageType = 1; // 1~6번째 문제는 promptType 1
                } else if (targetIndex < 13) {
                    messageType = 2; // 7~12번째 문제는 promptType 2
                } else if (targetIndex < 19) {
                    messageType = 3; // 13~18번째 문제는 promptType 3
                } else {
                    messageType = 4; // 19번째 이후 문제는 promptType 4
                }

                // targetIndex에 해당하는 문제를 생성
                List<String> nextQuestion = gptQuizService.generateQuestions(contentList.subList(targetIndex - 1, targetIndex), messageType, 1);
                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion); // 생성된 문제를 기존 문제 리스트에 추가
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0)); // 새로운 문제 로그 출력
                }

                session.setAttribute("generatedQuestions", generatedQuestions); // 세션에 업데이트된 문제 리스트 저장
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e); // 백그라운드에서 문제 생성 중 오류 발생 시 로그에 출력
            }
        }).start(); // 백그라운드 스레드에서 실행
    }

}
