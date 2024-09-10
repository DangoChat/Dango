package com.dangochat.dango.controller;

import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;

    //단어 20개 학습 하기
    @GetMapping("word")
    public String studyWord(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {

        // 로그인 된 유저 ID(int) 가져 오기
        int userId = userDetails.getId();
        log.debug("로그인 한 유저 아이디" + userId);

        // 한국어 능력 시험 level 2
        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevelAndType("N2", "단어",userId);
        log.debug("========" + studyContent.toString());
        model.addAttribute("studyContent", studyContent);
        model.addAttribute("userId", userId);  // userId를 모델에 추가
        return "StudyView/word";
    }

    // o/x 누르면 유저 공부 기록, 오답 노트 테이블에 저장 되는 거
    @ResponseBody
    @PostMapping("answer")
    public ResponseEntity<String> answer(
            @RequestParam("studyContentId") int studyContentId,
            @RequestParam("userId") Integer userId,
            @RequestParam("answer") String answer) {
        try {
            boolean isCorrect = "O".equals(answer);
            studyService.recordStudyContent(studyContentId, userId, isCorrect);

            if (!isCorrect) {
                studyService.recordMistake(userId, studyContentId);
            }

            return ResponseEntity.ok("정답이 성공적으로 저장 되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
        }
    }

    //학습 끝내기
    @PostMapping("complete")
    @ResponseBody
    public String completeStudy() {
        // 학습 완료 처리 로직 (DB 업데이트 등)
        return "{\"status\":\"success\"}";
    }

    // 문법 20개 학습 하기
    @GetMapping("grammar")
    public String grammar(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {

        // 로그인 된 유저 ID(int) 가져 오기
        int userId = userDetails.getId();
        log.debug("로그인 한 유저 아이디" + userId);

        // 한국어 능력 시험 level 2
        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevelAndType("N4",  "문법", userId);
        log.debug("========" + studyContent.toString());
        model.addAttribute("studyContent", studyContent);
        model.addAttribute("userId", userId);  // userId를 모델에 추가
        return "StudyView/grammar";
    }
    
    
    //오답노트 컨트롤러
    @GetMapping("mistakes")
    public String mistakes(Model model,@AuthenticationPrincipal AuthenticatedUser userDetails) {

    	// 로그인 된 유저 ID(int) 가져 오기
        int userId = userDetails.getId();
        
    	List<StudyEntity> userMistakes = studyService.mistakes(userId);
    	model.addAttribute("userMistakes", userMistakes);
       
        return "StudyView/mistakes";
    }
    
    @GetMapping("mistakes2")
    public String mistakes2(Model model,@AuthenticationPrincipal AuthenticatedUser userDetails) {

    	// 로그인 된 유저 ID(int) 가져 오기
        int userId = userDetails.getId();
        
    	List<StudyEntity> userMistakes = studyService.mistakes2(userId);
    	model.addAttribute("userMistakes", userMistakes);
       
        return "StudyView/mistakes2";
    }
}
