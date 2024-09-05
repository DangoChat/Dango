package com.dangochat.dango.controller;


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

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;
    @GetMapping("word")
    public String studyWord(Model model, @AuthenticationPrincipal AuthenticatedUser userDetails) {
//        String userId = userDetails.getUsername();  // 로그인된 유저 ID 가져오기
        // 학습 내용 가져오기 (레벨 2 기준)
            // AuthenticatedUser로 캐스팅하여 userId(int)를 가져옴
//            AuthenticatedUser authenticatedUser = (AuthenticatedUser) userDetails;
            int userId = userDetails.getId(); // 로그인된 유저 ID(int) 가져오기
        log.debug("========" + userId);

        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevel("2", userId);
            log.debug("========" + studyContent.toString());
            model.addAttribute("studyContent", studyContent);
            model.addAttribute("userId", userId);  // userId를 모델에 추가
            return "StudyView/word";
        }

        //o/x 누르면 유저공부기록, 오답노트 테이블에 저장되는 거
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

}

