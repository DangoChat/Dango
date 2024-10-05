package com.dangochat.dango.controller;

import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.dto.UserStudyContentDTO;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    
 
    
    @GetMapping("/wordReview")
    public String wordReview(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        
        // 유저가 학습한 모든 날짜 리스트를 가져옵니다.
        List<String> studyDates = studyService.getUserStudyDates(userId);
        
        // 데이터를 모델에 추가하여 HTML로 전달합니다.
        model.addAttribute("studyDates", studyDates);
        
        return "StudyView/wordReviewView"; // 학습 날짜에 맞는 페이지로 이동
    }
    
    @GetMapping("/wordReviewByDate")
    public String wordReviewByDate(@RequestParam("date") String date, Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        
        // String을 java.sql.Date로 변환
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date sqlDate = Date.valueOf(localDate);
        
        // 해당 날짜에 대한 유저의 학습 데이터를 가져옵니다.
        List<UserStudyContentDTO> studyList = studyService.getUserWordStudyContentByDate(userId, sqlDate);
        
        // 데이터를 모델에 추가하여 HTML로 전달합니다.
        model.addAttribute("studyList", studyList);
        model.addAttribute("selectedDate", date); // 선택된 날짜 표시
        
        return "StudyView/wordReviewView"; // 해당 날짜에 맞는 페이지로 이동
    }
    
    
   
    @GetMapping("/grammarReview")
    public String grammarReview(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
    	 
    	int userId = userDetails.getId();
        
        // 유저가 학습한 모든 날짜 리스트를 가져옵니다.
        List<String> studyDates = studyService.getUserStudyDates(userId);
        
        // 데이터를 모델에 추가하여 HTML로 전달합니다.
        model.addAttribute("studyDates", studyDates);
    	
        return "StudyView/grammarReviewView";  
    }
    
    @GetMapping("/grammarReviewByDate")
    public String grammarReviewByDate(@RequestParam("date") String date, Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        
        // String을 java.sql.Date로 변환
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date sqlDate = Date.valueOf(localDate);
        
        // 해당 날짜에 대한 유저의 학습 데이터를 가져옵니다.
        List<UserStudyContentDTO> studyList = studyService.getUserGrammarStudyContentByDate(userId, sqlDate);
        
        // 데이터를 모델에 추가하여 HTML로 전달합니다.
        model.addAttribute("studyList", studyList);
        model.addAttribute("selectedDate", date); // 선택된 날짜 표시
        
        return "StudyView/grammarReviewView"; // 해당 날짜에 맞는 페이지로 이동
    }
    
}
