package com.dangochat.dango.controller;

import com.dangochat.dango.dto.UserStudyContentDTO;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    
    // 날짜별로 '단어', '문법' 버튼이 있는 페이지로 이동
    
    @GetMapping("/studyReview")
    public String studyReview(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        
        return "StudyView/studyReviewView"; 
    }
    
    
    @GetMapping("/studyReviewByDateAndType")
    public String studyReviewByDateAndType(@RequestParam("date") String date, @RequestParam("type") String type, Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        
        // String을 java.sql.Date로 변환
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date sqlDate = Date.valueOf(localDate);
        
        List<UserStudyContentDTO> studyList;
        
        if ("word".equals(type)) {
            // 단어 데이터를 가져옴
            studyList = studyService.getUserWordStudyContentByDate(userId, sqlDate);
            // 데이터를 모델에 추가하여 HTML로 전달합니다.
            model.addAttribute("studyList", studyList);
            model.addAttribute("selectedDate", date);
            return "StudyView/wordReviewView";  // 단어 복습 페이지로 이동
        } else {
            // 문법 데이터를 가져옴
            studyList = studyService.getUserGrammarStudyContentByDate(userId, sqlDate);
            // 데이터를 모델에 추가하여 HTML로 전달합니다.
            model.addAttribute("studyList", studyList);
            model.addAttribute("selectedDate", date);
            return "StudyView/grammarReviewView";  // 문법 복습 페이지로 이동
        }
    }
    
    
}
