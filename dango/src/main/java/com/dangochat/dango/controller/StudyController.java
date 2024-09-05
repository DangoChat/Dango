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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

            List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevel("2", userId);
            log.debug("========" + studyContent.toString());
            model.addAttribute("studyContent", studyContent);
            model.addAttribute("userId", userId);  // userId를 모델에 추가
            return "StudyView/word";
        }

        @PostMapping("answer")
        public ResponseEntity<String> Answer ( @RequestParam("studyContentId") int studyContentId,
        @RequestParam("userId") int userId,
        @RequestParam("answer") String answer){
            try {
                // O 버튼을 누르면 true, X(외우지 않음) 버튼을 누르면 false로 처리
                boolean isCorrect = "O".equals(answer);

                // 항상 UserStudyContent에 저장
                studyService.recordStudyContent(studyContentId, userId, isCorrect);

                // X일 경우 UserMistakes에 저장
                if (!isCorrect) {
                    studyService.recordMistake(userId, studyContentId);
                }

                return ResponseEntity.ok("정답이 성공적으로 저장되었습니다.");
            } catch (Exception e) {
                log.error("정답 저장 중 오류 발생", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
            }
        }
    }

