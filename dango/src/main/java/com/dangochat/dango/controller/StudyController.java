package com.dangochat.dango.controller;


import com.dangochat.dango.dto.AnswerDTO;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;

    //단어 학습 하기
    @GetMapping("word")
    public String studyWord(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {

        // 로그인 된 유저 ID(int) 가져 오기
        int userId = userDetails.getId();
        log.debug("로그인 한 유저 아이디" + userId);

        // 한국어 능력 시험 level 2
        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevel("2", userId);
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
            log.debug("djflafjl");
            boolean isCorrect = "O".equals(answer);
            studyService.recordStudyContent(studyContentId, userId, isCorrect);

            if (!isCorrect) {
                studyService.recordMistake(userId, studyContentId);
            }

            return ResponseEntity.ok("정답이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
        }
    }

    @ResponseBody
    @PostMapping("finish")
    public ResponseEntity<String> finishStudy(@RequestBody List<AnswerDTO> answers) {
        try {
            for (AnswerDTO answer : answers) {
                boolean isCorrect = "O".equals(answer.getAnswer());
                studyService.recordStudyContent(answer.getStudyContentId(), answer.getUserId(), isCorrect);

                if (!isCorrect) {
                    studyService.recordMistake(answer.getUserId(), answer.getStudyContentId());
                }
            }
            return ResponseEntity.ok("모든 정답이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
        }
    }


    @PostMapping("complete")
    @ResponseBody
    public String completeStudy() {
        // 학습 완료 처리 로직 (DB 업데이트 등)
        return "{\"status\":\"success\"}";
    }

}

