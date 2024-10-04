package com.dangochat.dango.restcontroller;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTQuizService;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/quiz")  // REST API용으로 경로를 /api로 변경
@RequiredArgsConstructor
@Slf4j
public class GPTQuizRestController {

	private final GPTService gptService;
    private final GPTQuizService gptQuizService;
    private final StudyService studyService;
    private final MemberRepository memberRepository;

    // 첫 번째 단어 문제를 시작하는 API
    @GetMapping("/dailyWordTest/1")
    public ResponseEntity<Map<String, Object>> startDailyWordTest(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용
        String userNationality = member.getUserNationality();

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadDailyWordTestQuestions(session, 1, 3, userId, userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            response.put("question", generatedQuestions.get(0)); // 1번째 문제
            response.put("currentIndex", 1);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(response);  // JSON으로 반환
    }

    // '단어'만 가져올 수 있도록 기존의 loadInitialListeningQuestions 수정
    private void loadDailyWordTestQuestions(HttpSession session, int startIndex, int count, int userId, String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getTodayWordContent(userId); // '단어' 타입 콘텐츠만 가져온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int word = 2; // 서비스에 단어 관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex), word, count, userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 단어 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("단어 문제가 생성되지 않았습니다.", e);
        }
    }

    // 다음 문제로 이동하는 API
    @PostMapping("/dailyWordTest/next")
    public ResponseEntity<Map<String, Object>> nextDailyWordTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            currentIndex++;  // 인덱스 증가
            session.setAttribute("currentIndex", currentIndex);  // 세션에 업데이트된 인덱스 저장
            response.put("question", generatedQuestions.get(currentIndex - 1));  // 1-based index로 가져옴
            response.put("currentIndex", currentIndex);  // 클라이언트에 반환할 인덱스
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex);
            return ResponseEntity.ok(response);
        }

        // 마지막 문제일 때는 204 No Content 응답
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    // 특정 문제 번호로 접근하는 API
    @GetMapping("/dailyWordTest/{questionNumber}")
    public ResponseEntity<Map<String, Object>> dailyWordTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        Integer userId = userDetails.getId();
        session.setAttribute("userId", userId); // 세션에 userId 저장

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 20) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground1(session, messageType, questionNumber + 2, userId, userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return ResponseEntity.ok(response);
    }

    // 백그라운드에서 '단어'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground1(HttpSession session, int messageType, int targetIndex, int userId, String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getTodayWordContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int word = 2;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), word, 1, userNationality); // targetIndex번째 문제 생성

                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion);
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                }

                session.setAttribute("generatedQuestions", generatedQuestions);
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start(); // 백그라운드 스레드에서 실행
    }
    
    
    
    
    // 첫 번째 문법 문제를 시작하는 API
    @GetMapping("/dailyGrammarTest/1")
    public ResponseEntity<Map<String, Object>> startDailyGrammarTest(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용
        String userNationality = member.getUserNationality();

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadDailyGrammarTestQuestions(session, 1, 3, userId, userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            response.put("question", generatedQuestions.get(0)); // 1번째 문제
            response.put("currentIndex", 1);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(response);  // JSON으로 반환
    }

    // '문법'만 가져올 수 있도록 
    private void loadDailyGrammarTestQuestions(HttpSession session, int startIndex, int count, int userId, String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getTodayGrammarContent(userId); // '문법' 타입 콘텐츠만 가져온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int Grammar = 3; // 서비스에 단어 관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex), Grammar, count, userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 문법 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("단어 문제가 생성되지 않았습니다.", e);
        }
    }

    // 다음 문제로 이동하는 API
    @PostMapping("/dailyGrammarTest/next")
    public ResponseEntity<Map<String, Object>> nextDailyGrammarTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            currentIndex++;  // 인덱스 증가
            session.setAttribute("currentIndex", currentIndex);  // 세션에 업데이트된 인덱스 저장
            response.put("question", generatedQuestions.get(currentIndex - 1));  // 1-based index로 가져옴
            response.put("currentIndex", currentIndex);  // 클라이언트에 반환할 인덱스
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex);
            return ResponseEntity.ok(response);
        }

        // 마지막 문제일 때는 204 No Content 응답
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    // 특정 문제 번호로 접근하는 API
    @GetMapping("/dailyGrammarTest/{questionNumber}")
    public ResponseEntity<Map<String, Object>> dailyGrammarTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        Integer userId = userDetails.getId();
        session.setAttribute("userId", userId); // 세션에 userId 저장

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 20) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground2(session, messageType, questionNumber + 2, userId, userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return ResponseEntity.ok(response);
    }

    // 백그라운드에서 '문법'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground2(HttpSession session, int messageType, int targetIndex, int userId, String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getTodayGrammarContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int grammar = 3;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), grammar, 1, userNationality); // targetIndex번째 문제 생성

                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion);
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                }

                session.setAttribute("generatedQuestions", generatedQuestions);
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start(); // 백그라운드 스레드에서 실행
    }
    
    
    
 // 첫 번째 단어 문제를 시작하는 API
    @GetMapping("/weeklyWordTest/1")
    public ResponseEntity<Map<String, Object>> startWeeklyWordTest(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용
        String userNationality = member.getUserNationality();

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadWeeklyWordTestQuestions(session, 1, 3, userId, userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            response.put("question", generatedQuestions.get(0)); // 1번째 문제
            response.put("currentIndex", 1);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(response);  // JSON으로 반환
    }

    // '단어'만 가져올 수 있도록
    private void loadWeeklyWordTestQuestions(HttpSession session, int startIndex, int count, int userId, String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getWeekWordContent(userId); // '단어' 타입 콘텐츠만 가져온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int word = 2; // 서비스에 단어 관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex), word, count, userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 단어 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("단어 문제가 생성되지 않았습니다.", e);
        }
    }
    
    // 다음 문제로 이동하는 API
    @PostMapping("/weeklyWordTest/next")
    public ResponseEntity<Map<String, Object>> nextWeeklyWordTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            currentIndex++;  // 인덱스 증가
            session.setAttribute("currentIndex", currentIndex);  // 세션에 업데이트된 인덱스 저장
            response.put("question", generatedQuestions.get(currentIndex - 1));  // 1-based index로 가져옴
            response.put("currentIndex", currentIndex);  // 클라이언트에 반환할 인덱스
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex);
            return ResponseEntity.ok(response);
        }

        // 마지막 문제일 때는 204 No Content 응답
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    // 특정 문제 번호로 접근하는 API
    @GetMapping("/weeklyWordTest/{questionNumber}")
    public ResponseEntity<Map<String, Object>> weeklyWordTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        Integer userId = userDetails.getId();
        session.setAttribute("userId", userId); // 세션에 userId 저장

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 20) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground3(session, messageType, questionNumber + 2, userId, userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return ResponseEntity.ok(response);
    }

    // 백그라운드에서 '단어'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground3(HttpSession session, int messageType, int targetIndex, int userId, String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getWeekWordContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int word = 2;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), word, 1, userNationality); // targetIndex번째 문제 생성

                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion);
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                }

                session.setAttribute("generatedQuestions", generatedQuestions);
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start(); // 백그라운드 스레드에서 실행
    }
    
    
 // 첫 번째 문법 문제를 시작하는 API
    @GetMapping("/weeklyGrammarTest/1")
    public ResponseEntity<Map<String, Object>> startWeeklyGrammarTest(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용
        String userNationality = member.getUserNationality();

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadWeeklyGrammarTestQuestions(session, 1, 3, userId, userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            response.put("question", generatedQuestions.get(0)); // 1번째 문제
            response.put("currentIndex", 1);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(response);  // JSON으로 반환
    }

    // '문법'만 가져올 수 있도록 
    private void loadWeeklyGrammarTestQuestions(HttpSession session, int startIndex, int count, int userId, String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getWeekGrammarContent(userId); // '문법' 타입 콘텐츠만 가져온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int Grammar = 3; // 서비스에 단어 관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex), Grammar, count, userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 문법 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("단어 문제가 생성되지 않았습니다.", e);
        }
    }

    // 다음 문제로 이동하는 API
    @PostMapping("/weeklyGrammarTest/next")
    public ResponseEntity<Map<String, Object>> nextWeeklyGrammarTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            currentIndex++;  // 인덱스 증가
            session.setAttribute("currentIndex", currentIndex);  // 세션에 업데이트된 인덱스 저장
            response.put("question", generatedQuestions.get(currentIndex - 1));  // 1-based index로 가져옴
            response.put("currentIndex", currentIndex);  // 클라이언트에 반환할 인덱스
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex);
            return ResponseEntity.ok(response);
        }

        // 마지막 문제일 때는 204 No Content 응답
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    // 특정 문제 번호로 접근하는 API
    @GetMapping("/weeklyGrammarTest/{questionNumber}")
    public ResponseEntity<Map<String, Object>> weeklyGrammarTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        Integer userId = userDetails.getId();
        session.setAttribute("userId", userId); // 세션에 userId 저장

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 21) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground4(session, messageType, questionNumber + 2, userId, userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return ResponseEntity.ok(response);
    }

    // 백그라운드에서 '문법'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground4(HttpSession session, int messageType, int targetIndex, int userId, String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getWeekGrammarContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int grammar = 3;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), grammar, 1, userNationality); // targetIndex번째 문제 생성

                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion);
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0));
                }
                
                session.setAttribute("generatedQuestions", generatedQuestions);
            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start(); // 백그라운드 스레드에서 실행
    }
    
    
    
    // 첫 번째 청해 문제를 시작하는 API
    @GetMapping("/listening/1")
    public ResponseEntity<Map<String, Object>> startListeningTest(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) throws IOException, MessagingException {
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1);
        session.setAttribute("currentMessageType", 1);

        log.info("초기 3개의 문제 생성 시작.");
        loadInitialListeningQuestions(session, 1, 3, userId, userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            response.put("question", generatedQuestions.get(0));
            response.put("currentIndex", 1);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok(response);  // JSON 형식으로 반환
    }

    // 청해 문제를 미리 생성하는 메서드
    private void loadInitialListeningQuestions(HttpSession session, int startIndex, int count, int userId, String userNationality) {
        List<String> studyContent = studyService.studyContentForToday(userId); // 유저 ID를 이용해 학습 내용 가져오기
        List<String> generatedQuestions = new ArrayList<>();
        int listening = 1;

        try {
            int messageType = (int) session.getAttribute("currentMessageType");
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());

            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex), listening, count, userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 청해 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("청해 문제가 생성되지 않았습니다.", e);
        }
    }

    // 다음 청해 문제로 이동하는 API
    @PostMapping("/listening/next")
    public ResponseEntity<Map<String, Object>> nextListeningQuestion(HttpSession session, @RequestParam("staytus") boolean staytus, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        if (!staytus) {
            user.setUserMileage(user.getUserMileage() + 1);
        } else {
            user.setUserMileage(user.getUserMileage() + 3);
        }
        memberRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            currentIndex++;
            session.setAttribute("currentIndex", currentIndex);
            response.put("question", generatedQuestions.get(currentIndex - 1));
            response.put("currentIndex", currentIndex);
            log.info("다음 청해 문제로 이동, 현재 인덱스: {}", currentIndex);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 특정 청해 문제 번호로 접근하는 API
    @GetMapping("/listening/{questionNumber}")
    public ResponseEntity<Map<String, Object>> listeningTestWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        String userNationality = member.getUserNationality();

        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        Map<String, Object> response = new HashMap<>();
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 유효하지 않으면 BAD REQUEST 반환
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1);
            response.put("question", currentQuestion);
            response.put("currentIndex", questionNumber);
            session.setAttribute("currentIndex", questionNumber);
            log.info("현재 청해 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1;
            session.setAttribute("currentMessageType", messageType);
        }

        Integer userId = userDetails.getId();
        session.setAttribute("userId", userId);

        if (questionNumber + 2 <= 23) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextListeningQuestionInBackground5(session, messageType, questionNumber + 2, userId, userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return ResponseEntity.ok(response);
    }

    // 백그라운드에서 청해 문제 생성하는 메서드
    private void generateNextListeningQuestionInBackground5(HttpSession session, int messageType, int targetIndex, int userId, String userNationality) {
        new Thread(() -> {
            try {
                List<String> studyContent = studyService.studyContentForToday(userId);
                int endIndex = Math.min(targetIndex, studyContent.size());

                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), messageType, 1, userNationality);
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

