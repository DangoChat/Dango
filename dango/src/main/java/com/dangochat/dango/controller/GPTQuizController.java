package com.dangochat.dango.controller;

import com.dangochat.dango.service.GPTQuizService;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.StudyService;
import com.dangochat.dango.service.userQuizQuestionReviewService;
import com.dangochat.dango.dto.UserQuizQuestionReviewDTO;
import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.entity.QuizType;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.security.AuthenticatedUser;

import jakarta.mail.MessagingException;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * startIndex: 지금 보고 있는 페이지의 문제 번호 (예: n번째 문제).
 * targetIndex: 백그라운드에서 생성해야 할 문제 번호 (예: n+2번째 문제).
 * currentIndex: 현재 사용자가 보고 있는 문제의 번호.
 * currentMessageType: 현재 문제의 문제유형(1,2,3,4)을 지정하는 변수
 * count: 생성할 문제의 개수 (예: 한 번에 몇 개의 문제를 생성할지 결정)
 * generatedQuestions: 세션에서 관리하는 만들어진 문제들의 리스트.
 * questionNumber: URL에서 받아온 문제 번호 (사용자가 선택한 문제).
 * contentList: 데이터베이스에서 가져온 랜덤한 단어 목록 (문제 생성을 위한 데이터).
 */
@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/quiz")
public class GPTQuizController {

    private final GPTQuizService gptQuizService;
    private final StudyRepository studyRepository;
    private final StudyService studyService; //안호꺼(그 날 배운 학습 내용 가저오는 메서드)
    private final GPTService gptService; //안호꺼(청해 gpt 문제 만드는 메서그)
    private final MemberService memberService;
    private final userQuizQuestionReviewService userQuizQuestionReviewService;
    private final MemberRepository memberRepository;

    


    //       =======================================================================================================

    // GPT로 청해문제 만드는 controller(이안호)
    @GetMapping("/listening/1")
    public String listeningLevel1(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
        
        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);
        

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadInitialListeningQuestions(session, 1, 3, userId,userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");
        
        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 청해 문제 표시: {}", currentQuestion);
        }

        return "QuizView/listening";  // 해당 뷰로 이동
    }


    private void loadInitialListeningQuestions(HttpSession session, int startIndex, int count, int userId,String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.studyContentForToday(userId); // 유저 ID를 이용해 학습 내용 가져오기
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int listening = 1; // 서비스에 듣기관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex),listening,count,userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 청해 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("청해 문제가 생성되지 않았습니다.", e);
        }
    }


    @PostMapping("/listening/next")
    public String nextListeningQuestion(HttpSession session, @RequestParam("staytus") boolean staytus, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	
    	 // 현재 로그인된 사용자의 ID를 가져오기 (마일리지 추가를위한 로직)
        int userId = userDetails.getId(); // AuthenticatedUser에서 userId를 가져옴
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
    	
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        
        if(!staytus) {
        	user.setUserMileage(user.getUserMileage() + 1);
        }else {
        	user.setUserMileage(user.getUserMileage() + 3);
        }
        memberRepository.save(user);
        
        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 청해 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 첫 번째 문제로 돌아가거나 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= generatedQuestions.size()) {
            return "redirect:/";  // 예시로 첫 문제로 돌아가도록 설정
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/listening/" + (currentIndex + 1);
    }

    @GetMapping("/listening/{questionNumber}")
    public String listeningLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
    	
    	List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return "redirect:/listening/1"; // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 청해 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // currentMessageType을 안전하게 Integer로 변환
        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        // userId를 인증된 사용자로부터 가져오고 세션에 저장
        Integer userId = userDetails != null ? userDetails.getId() : null;
        if (userId == null) {
            log.error("인증된 사용자가 없습니다.");
            return "redirect:/login"; // 사용자 인증 문제가 발생할 경우 로그인 페이지로 리다이렉트
        } else {
            session.setAttribute("userId", userId); // 세션에 userId 저장
        }

        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 23) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground2(session, messageType, questionNumber + 2, userId,userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/listening";
    }



    private void generateNextQuestionInBackground2(HttpSession session, int messageType, int targetIndex, int userId,String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 현재 레벨 가져오기 (userId를 사용하여)
                String userLevel = memberService.getUserCurrentLevel(userId);

                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.studyContentForToday(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정

                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, endIndex), messageType, 1,userNationality); // targetIndex번째 문제 생성

                // 세션에서 기존 문제 리스트를 가져옴
                List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

                if (generatedQuestions != null) {
                    generatedQuestions.addAll(nextQuestion); // 생성된 문제를 기존 문제 리스트에 추가
                    log.info("대기 중인 문제 추가: {}번째 문제 - {}", targetIndex, nextQuestion.get(0)); // 로그 출력
                }
                session.setAttribute("generatedQuestions", generatedQuestions);

            } catch (Exception e) {
                log.error("백그라운드에서 문제 생성 중 오류 발생: ", e);
            }
        }).start(); // 백그라운드 스레드에서 실행
    }





    // GPT로 일일 단어문제 만드는 controller(이안호)
    @GetMapping("/dailyWordTest/1")
    public String dailyWordTest(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
        
        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadDailyWordTestQuestions(session, 1, 3, userId,userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 청해 문제 표시: {}", currentQuestion);
        }

        return "QuizView/dailyWordTest";  // 해당 뷰로 이동
    }


    // '단어'만 가저올 수 있도록 기존의 loadInitialListeningQuestions 수정
    private void loadDailyWordTestQuestions(HttpSession session, int startIndex, int count, int userId,String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getTodayWordContent(userId); // '단어' 타입 콘텐츠만 가저온다
        System.out.println("Study content: " + studyContent);
        
        List<String> generatedQuestions = new ArrayList<>();
        int word = 2; // 서비스에 단어관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex),word,count,userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 단어 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("단어 문제가 생성되지 않았습니다.", e);
        }
    }



    @PostMapping("/dailyWordTest/next")
    public String nextDailyWordTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 첫 번째 문제로 돌아가거나 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= generatedQuestions.size()) {
            return "redirect:/";  // 예시로 첫 문제로 돌아가도록 설정
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/dailyWordTest/" + (currentIndex + 1);
    }

    @GetMapping("/dailyWordTest/{questionNumber}")
    public String dailyWordTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
    	
    	List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return "redirect:/dailyWordTest/1"; // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // currentMessageType을 안전하게 Integer로 변환
        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        // userId를 인증된 사용자로부터 가져오고 세션에 저장
        Integer userId = userDetails != null ? userDetails.getId() : null;
        if (userId == null) {
            log.error("인증된 사용자가 없습니다.");
            return "redirect:/login"; // 사용자 인증 문제가 발생할 경우 로그인 페이지로 리다이렉트
        } else {
            session.setAttribute("userId", userId); // 세션에 userId 저장
        }



        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 20) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground3(session, messageType, questionNumber + 2, userId,userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/dailyWordTest";
    }


    // 백그라운드에서 '단어'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground3(HttpSession session, int messageType, int targetIndex, int userId,String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getTodayWordContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int word = 2;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, targetIndex), word, 1,userNationality); // targetIndex번째 문제 생성

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





    // GPT로 일일 문법문제 만드는 controller(이안호)
    @GetMapping("/dailyGrammarTest/1")
    public String dailyGrammarTest(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationalityString userNationality = userDetails.getUserNationality(); // user_nationality 가져오기

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadDailyGrammarQuestions(session, 1, 3, userId,userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 청해 문제 표시: {}", currentQuestion);
        }

        return "QuizView/dailyGrammarTest";  // 해당 뷰로 이동
    }

    // '단어'만 가저올 수 있도록 기존의 loadInitialListeningQuestions 수정
    private void loadDailyGrammarQuestions(HttpSession session, int startIndex, int count, int userId,String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getTodayGrammarContent(userId); // '문법' 타입 콘텐츠만 가저온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int grammar = 3; // 서비스에 문법관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex),grammar,count,userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 청해 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("청해 문제가 생성되지 않았습니다.", e);
        }
    }



    @PostMapping("/dailyGrammarTest/next")
    public String nextDailyGrammarTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 첫 번째 문제로 돌아가거나 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= generatedQuestions.size()) {
            return "redirect:/";  // 예시로 첫 문제로 돌아가도록 설정
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/dailyGrammarTest/" + (currentIndex + 1);
    }

    @GetMapping("/dailyGrammarTest/{questionNumber}")
    public String dailyGrammarTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
    	
    	List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return "redirect:/dailyWordTest/1"; // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // currentMessageType을 안전하게 Integer로 변환
        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        // userId를 인증된 사용자로부터 가져오고 세션에 저장
        Integer userId = userDetails != null ? userDetails.getId() : null;
        if (userId == null) {
            log.error("인증된 사용자가 없습니다.");
            return "redirect:/login"; // 사용자 인증 문제가 발생할 경우 로그인 페이지로 리다이렉트
        } else {
            session.setAttribute("userId", userId); // 세션에 userId 저장
        }



        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 3) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground4(session, messageType, questionNumber + 2, userId,userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/dailyGrammarTest";
    }


    // 백그라운드에서 '문법'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground4(HttpSession session, int messageType, int targetIndex, int userId,String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getTodayGrammarContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int grammar = 3;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, targetIndex), grammar, 1,userNationality); // targetIndex번째 문제 생성

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




    // GPT로 주간 단어문제 만드는 controller(이안호)
    @GetMapping("/weeklyWordTest/1")
    public String weeklyWordTest(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
        
        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadWeeklyWordQuestions(session, 1, 3, userId,userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 청해 문제 표시: {}", currentQuestion);
        }

        return "QuizView/weeklyWordTest";  // 해당 뷰로 이동
    }


    // '단어'만 가저올 수 있도록 기존의 loadInitialListeningQuestions 수정
    private void loadWeeklyWordQuestions(HttpSession session, int startIndex, int count, int userId,String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getWeekWordContent(userId); // '단어' 타입 콘텐츠만 가저온다 (주간)
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int word = 2; // 서비스에 단어관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex),word,count,userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 청해 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("청해 문제가 생성되지 않았습니다.", e);
        }
    }


    @PostMapping("/weeklyWordTest/next")
    public String nextWeeklyWordTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 첫 번째 문제로 돌아가거나 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= generatedQuestions.size()) {
            return "redirect:/";  // 예시로 첫 문제로 돌아가도록 설정
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/weeklyWordTest/" + (currentIndex + 1);
    }

    @GetMapping("/weeklyWordTest/{questionNumber}")
    public String weeklyWordTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
    	
    	List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return "redirect:/weeklyWordTest/1"; // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 단어 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // currentMessageType을 안전하게 Integer로 변환
        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        // userId를 인증된 사용자로부터 가져오고 세션에 저장
        Integer userId = userDetails != null ? userDetails.getId() : null;
        if (userId == null) {
            log.error("인증된 사용자가 없습니다.");
            return "redirect:/login"; // 사용자 인증 문제가 발생할 경우 로그인 페이지로 리다이렉트
        } else {
            session.setAttribute("userId", userId); // 세션에 userId 저장
        }



        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 20) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground5(session, messageType, questionNumber + 2, userId,userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/dailyWordTest";
    }


    // 백그라운드에서 '단어'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground5(HttpSession session, int messageType, int targetIndex, int userId,String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getWeekWordContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int word = 2;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, targetIndex), word, 1,userNationality); // targetIndex번째 문제 생성

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




    // GPT로 주간 단어문제 만드는 controller(이안호)
    @GetMapping("/weeklyGrammarTest/1")
    public String weeklyGrammarTest(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) throws IOException, MessagingException {
        // 로그인 된 유저 ID 가져오기
        int userId = userDetails.getId();
        MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality

        // 세션 초기화 후 첫 번째 문제부터 시작
        session.setAttribute("generatedQuestions", new ArrayList<String>());
        session.setAttribute("currentIndex", 1); // 첫 번째 문제로 설정
        session.setAttribute("currentMessageType", 1);

        // 3개의 문제를 미리 생성해서 세션에 저장
        log.info("초기 3개의 문제 생성 시작.");
        loadWeeklyGrammarQuestions(session, 1, 3, userId,userNationality);  // 첫 번째 문제에서 3개의 문제 생성
        log.info("초기 3개의 문제 생성 완료.");

        // 첫 번째 문제를 가져와서 화면에 표시
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        if (generatedQuestions != null && !generatedQuestions.isEmpty()) {
            String currentQuestion = generatedQuestions.get(0); // 1번째 문제
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", 1); // 사용자에게는 1번째 문제로 보여줌
            log.info("첫 번째 청해 문제 표시: {}", currentQuestion);
        }

        return "QuizView/weeklyGrammarTest";  // 해당 뷰로 이동
    }


    // '문법'만 가저올 수 있도록 기존의 loadInitialListeningQuestions 수정
    private void loadWeeklyGrammarQuestions(HttpSession session, int startIndex, int count, int userId,String userNationality) {
        // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
        List<String> studyContent = studyService.getWeekGrammarContent(userId); // '문법' 타입 콘텐츠만 가저온다
        System.out.println("Study content: " + studyContent);

        List<String> generatedQuestions = new ArrayList<>();
        int grammar = 3; // 서비스에 문법관련 문제를 요청하는 정수
        try {
            int messageType = (int) session.getAttribute("currentMessageType");  // 명시적 형변환

            // studyContent의 크기를 확인하여 범위를 조정
            int endIndex = Math.min(startIndex - 1 + count, studyContent.size());
            // subList 범위가 리스트 크기를 넘지 않도록 안전하게 처리
            if (startIndex - 1 < studyContent.size()) {
                generatedQuestions = gptService.generateGPTQuestions(studyContent.subList(startIndex - 1, endIndex),grammar,count,userNationality);
            } else {
                log.warn("startIndex가 studyContent의 크기를 초과했습니다.");
            }

            session.setAttribute("generatedQuestions", generatedQuestions);
            log.info("초기 생성된 {}개의 청해 문제: {}", count, generatedQuestions);
        } catch (Exception e) {
            log.error("청해 문제가 생성되지 않았습니다.", e);
        }
    }


    @PostMapping("/weeklyGrammarTest/next")
    public String nextWeeklyGrammarTestQuestion(HttpSession session) {
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");
        List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");

        // 다음 문제로 인덱스 증가
        if (currentIndex != null && generatedQuestions != null && currentIndex < generatedQuestions.size()) {
            session.setAttribute("currentIndex", currentIndex + 1);
            log.info("다음 단어 문제로 이동, 현재 인덱스: {}", currentIndex + 1);
        }

        // 마지막 문제일 때는 첫 번째 문제로 돌아가거나 홈으로 리다이렉트
        if (currentIndex != null && currentIndex >= generatedQuestions.size()) {
            return "redirect:/";  // 예시로 첫 문제로 돌아가도록 설정
        }

        // 다음 문제 화면에 출력 (URL에 문제 번호를 포함)
        return "redirect:/quiz/weeklyGrammarTest/" + (currentIndex + 1);
    }

    @GetMapping("/weeklyGrammarTest/{questionNumber}")
    public String weeklyGrammarTestLevelWithQuestionNumber(@PathVariable("questionNumber") int questionNumber, Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	MemberEntity member = memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 가져온 MemberEntity에서 user_nationality 값을 사용합니다.
        String userNationality = member.getUserNationality();  // 데이터베이스에서 가져온 user_nationality
    	
    	List<String> generatedQuestions = (List<String>) session.getAttribute("generatedQuestions");
        Integer currentIndex = (Integer) session.getAttribute("currentIndex");

        // 유효한 문제 번호인지 확인
        if (generatedQuestions == null || questionNumber > generatedQuestions.size() || questionNumber < 1) {
            return "redirect:/weeklyGrammarTest/1"; // 범위를 벗어나면 첫 문제로 리다이렉트
        }

        if (generatedQuestions != null && questionNumber <= generatedQuestions.size()) {
            String currentQuestion = generatedQuestions.get(questionNumber - 1); // 1-based index
            model.addAttribute("question", currentQuestion);
            model.addAttribute("currentIndex", questionNumber);  // 사용자에게는 1-based index로 보여줌
            session.setAttribute("currentIndex", questionNumber); // 세션에 현재 문제 번호 저장
            log.info("현재 문법 문제 표시: {}번째 문제 - {}", questionNumber, currentQuestion);
        }

        // currentMessageType을 안전하게 Integer로 변환
        Integer messageType = (Integer) session.getAttribute("currentMessageType");
        if (messageType == null) {
            messageType = 1; // 기본값 설정 (예: 1)
            session.setAttribute("currentMessageType", messageType); // 세션에 저장
        }

        // userId를 인증된 사용자로부터 가져오고 세션에 저장
        Integer userId = userDetails != null ? userDetails.getId() : null;
        if (userId == null) {
            log.error("인증된 사용자가 없습니다.");
            return "redirect:/login"; // 사용자 인증 문제가 발생할 경우 로그인 페이지로 리다이렉트
        } else {
            session.setAttribute("userId", userId); // 세션에 userId 저장
        }
        
        
        
        // n번째 문제를 풀 때 n+2번째 문제를 백그라운드에서 미리 생성
        if (questionNumber + 2 <= 7) {
            log.info("{}번째 문제 이후에 {}번째 문제를 생성 중...", questionNumber, questionNumber + 2);
            generateNextQuestionInBackground6(session, messageType, questionNumber + 2, userId,userNationality);
            log.info("{}번째 문제 생성 완료.", questionNumber + 2);
        }

        return "QuizView/weeklyGrammarTest";
    }


    // 백그라운드에서 '문법'관련 문제를 만들어주는 메서드
    private void generateNextQuestionInBackground6(HttpSession session, int messageType, int targetIndex, int userId,String userNationality) {
        new Thread(() -> {
            try {
                // 유저의 학습 콘텐츠를 가져오기 위해 studyService 사용
                List<String> studyContent = studyService.getWeekGrammarContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
                int endIndex = Math.min(targetIndex, studyContent.size()); // studyContent의 크기 넘지 않도록 설정
                int grammar = 3;
                List<String> nextQuestion = gptService.generateGPTQuestions(studyContent.subList(targetIndex - 1, targetIndex), grammar, 1,userNationality); // targetIndex번째 문제 생성

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

    
    
    
// ================   user_quiz_question_review에 시험본 정보 넣는 로직
    
    @PostMapping("/dailyWordTest/save")
    @ResponseBody  // Ajax 응답을 위해 추가
    public Map<String, Object> saveQuizQuestion(@RequestParam("quizContent") String quizContent, 
                                                @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	
        Map<String, Object> response = new HashMap<>();
        try {
            // DTO 생성
            UserQuizQuestionReviewDTO reviewDTO = new UserQuizQuestionReviewDTO();
            reviewDTO.setUserId(userDetails.getId());  // 현재 사용자 ID 설정
            reviewDTO.setQuizType(QuizType.daily);  // 퀴즈 타입 설정
            reviewDTO.setQuizContent(quizContent);  // 퀴즈 내용 설정
            reviewDTO.setQuizStatus(true);  // 상태 설정

            // 서비스 호출하여 퀴즈 저장
            userQuizQuestionReviewService.saveUserQuizQuestion(reviewDTO);

            response.put("status", "success");
            response.put("message", "퀴즈가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("퀴즈 저장 중 오류 발생", e);
            response.put("status", "error");
            response.put("message", "퀴즈 저장 중 오류가 발생했습니다.");
        }
        return response;  // JSON 형식으로 응답
    }
    
    
    
    @PostMapping("/dailyGrammarTest/save")
    @ResponseBody
    public Map<String, Object> saveQuizQuestion2(@RequestParam("quizContent") String quizContent, 
                                                @AuthenticationPrincipal AuthenticatedUser userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            // DTO 생성
            UserQuizQuestionReviewDTO reviewDTO = new UserQuizQuestionReviewDTO();
            reviewDTO.setUserId(userDetails.getId());  // 현재 사용자 ID 설정
            reviewDTO.setQuizType(QuizType.daily);  // 퀴즈 타입 설정
            reviewDTO.setQuizContent(quizContent);  // 퀴즈 내용 설정
            reviewDTO.setQuizStatus(true);  // 상태 설정

            // 서비스 호출하여 퀴즈 저장
            userQuizQuestionReviewService.saveUserQuizQuestion(reviewDTO);

            response.put("status", "success");
            response.put("message", "퀴즈가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("퀴즈 저장 중 오류 발생: {}", e.getMessage(), e);  // 예외 메시지와 스택 트레이스를 로그에 출력
            response.put("status", "error");
            response.put("message", "퀴즈 저장 중 오류가 발생했습니다: " + e.getMessage());  // 클라이언트에 좀 더 구체적인 메시지를 전달
        }
        return response;  // JSON 형식으로 응답
    }

    
    
    @PostMapping("/weeklyWordTest/save")
    @ResponseBody  // Ajax 응답을 위해 추가
    public Map<String, Object> saveQuizQuestion3(@RequestParam("quizContent") String quizContent, 
                                                @AuthenticationPrincipal AuthenticatedUser userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            // DTO 생성
            UserQuizQuestionReviewDTO reviewDTO = new UserQuizQuestionReviewDTO();
            reviewDTO.setUserId(userDetails.getId());  // 현재 사용자 ID 설정
            reviewDTO.setQuizType(QuizType.weekly);  // 퀴즈 타입 설정
            reviewDTO.setQuizContent(quizContent);  // 퀴즈 내용 설정
            reviewDTO.setQuizStatus(true);  // 상태 설정

            // 서비스 호출하여 퀴즈 저장
            userQuizQuestionReviewService.saveUserQuizQuestion(reviewDTO);

            response.put("status", "success");
            response.put("message", "퀴즈가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("퀴즈 저장 중 오류 발생", e);
            response.put("status", "error");
            response.put("message", "퀴즈 저장 중 오류가 발생했습니다.");
        }
        return response;  // JSON 형식으로 응답
    }
    
    
    
    @PostMapping("/weeklyGrammarTest/save")
    @ResponseBody
    public Map<String, Object> saveQuizQuestion4(@RequestParam("quizContent") String quizContent, 
                                                @AuthenticationPrincipal AuthenticatedUser userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            // DTO 생성
            UserQuizQuestionReviewDTO reviewDTO = new UserQuizQuestionReviewDTO();
            reviewDTO.setUserId(userDetails.getId());  // 현재 사용자 ID 설정
            reviewDTO.setQuizType(QuizType.daily);  // 퀴즈 타입 설정
            reviewDTO.setQuizContent(quizContent);  // 퀴즈 내용 설정
            reviewDTO.setQuizStatus(true);  // 상태 설정

            // 서비스 호출하여 퀴즈 저장
            userQuizQuestionReviewService.saveUserQuizQuestion(reviewDTO);

            response.put("status", "success");
            response.put("message", "퀴즈가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("퀴즈 저장 중 오류 발생: {}", e.getMessage(), e);  // 예외 메시지와 스택 트레이스를 로그에 출력
            response.put("status", "error");
            response.put("message", "퀴즈 저장 중 오류가 발생했습니다: " + e.getMessage());  // 클라이언트에 좀 더 구체적인 메시지를 전달
        }
        return response;  // JSON 형식으로 응답
    }
}
