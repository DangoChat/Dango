package com.dangochat.dango.controller;

import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.entity.QuizType;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserQuizQuestionReviewEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;
    private final GPTService gptService;

    @GetMapping("/word")
    public String studyRestWord() {
        return "StudyView/restWord";
    }
    @GetMapping("/grammar")
    public String studyRestGrammar() {
        return "StudyView/restGrammar";
    }
    @GetMapping("/wordMistakes")
    public String studyRestWordMistakes() {
        return "StudyView/restWordMistakes";
    }
    @GetMapping("/grammarMistakes")
    public String studyRestGrammarMistakes() {
        return "StudyView/restGrammarMistakes";
    }
   
    
    // 유저 일간,주간 테스트 기록 관련
    @GetMapping("/dailyTestView")
    public String dailyTestView(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {
        // 로그인된 유저 ID를 가져옵니다.
        int userId = userDetails.getId();
        
        // QuizType.DAILY를 직접 사용
        List<UserQuizQuestionReviewEntity> dailyTests = studyService.findQuizByTypeAndUserId(QuizType.daily, userId);
        // 조회한 데이터를 모델에 추가하여 뷰에 전달
        
        model.addAttribute("dailyTests", dailyTests);

        return "StudyView/dailyTestView";  // dailyTestView.html로 이동
    }
    
    @GetMapping("/weeklyTestView")
    public String weeklyTestView(@AuthenticationPrincipal AuthenticatedUser userDetails, Model model) {
    	 // 로그인된 유저 ID를 가져옵니다.
        int userId = userDetails.getId();
        
        // QuizType.DAILY를 직접 사용
        List<UserQuizQuestionReviewEntity> weeklyTests = studyService.findQuizByTypeAndUserId(QuizType.weekly, userId);
        // 조회한 데이터를 모델에 추가하여 뷰에 전달
        
        model.addAttribute("weeklyTests", weeklyTests);
    	
    	
        return "StudyView/weeklyTestView";
    }

}
