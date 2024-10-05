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
    
    
// ============================================================    
    
    // 유저 일간,주간 테스트 기록 관련 Controller
    @GetMapping("/dailyTestView")
    public String dailyTestView() {
        return "StudyView/dailyTestView";  
    }

    @GetMapping("/weeklyTestView")
    public String weeklyTestView() {
        return "StudyView/weeklyTestView";
    }

    
    
    
// =========================================================== 
    
    // 유저 단어,문법 복습 Controller
    @GetMapping("/wordReview")
    public String wordReview(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	
    	 // 로그인한 유저의 ID를 가져옵니다.
        int userId = userDetails.getId();
        
        // 유저의 학습 기록을 가져옵니다.
      //  List<StudyDTO> userStudyContent = studyService.getStudyContentByUserId(userId);

        // 뷰로 전달하기 위해 모델에 데이터를 추가합니다.
      //  model.addAttribute("userStudyContent", userStudyContent);
    	
        return "StudyView/wordReviewView";  
    }
   
    @GetMapping("/grammarReview")
    public String grammarReview() {
    	
    	
        return "StudyView/grammarReviewView";  
    }
    
}
