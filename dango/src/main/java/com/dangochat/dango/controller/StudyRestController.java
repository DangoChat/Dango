package com.dangochat.dango.controller;

import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/study")
public class StudyRestController {

    private final StudyService studyService;
    private final GPTService gptService;

    // 단어 20개 학습하기
    @GetMapping("word")
    public ResponseEntity<?> studyWord(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        // 로그인된 유저 ID(int) 가져오기
        int userId = userDetails.getId();
        log.debug("로그인한 유저 아이디: " + userId);

        String userlevel = studyService.getUserLevel(userId);  // 사용자 레벨 가져오기

        // StudyEntity를 StudyDTO로 변환하여 리스트로 저장
        List<StudyDTO> studyContent = studyService.getRandomStudyContentByLevelAndType(userlevel, "단어", userId)
                .stream()
                .map(studyEntity -> StudyDTO.builder()
                        .studyContentId(studyEntity.getStudyContentId())
                        .content(studyEntity.getContent())
                        .pronunciation(studyEntity.getPronunciation())
                        .meaning(studyEntity.getMeaning())
                        .type(studyEntity.getType())
                        .level(studyEntity.getLevel())
                        .example1(studyEntity.getExample1())
                        .exampleTranslation1(studyEntity.getExampleTranslation1())
                        .example2(studyEntity.getExample2())
                        .exampleTranslation2(studyEntity.getExampleTranslation2())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok().body(studyContent);
    }

    // O/X 눌렀을 때 유저 공부 기록 및 오답 노트에 저장
    @PostMapping("answer")
    public ResponseEntity<String> answer(
            @RequestParam("studyContentId") int studyContentId,
            @RequestParam("userId") Integer userId,
            @RequestParam("answer") String answer,
            @RequestParam("studyType") String studyType) {

        try {
            boolean isCorrect = "O".equals(answer);
            studyService.recordStudyContent(studyContentId, userId, isCorrect, studyType);

            if (!isCorrect) {
                studyService.recordMistake(userId, studyContentId);
            }

            return ResponseEntity.ok("정답이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
        }
    }

    // 학습 완료 처리
    @PostMapping("complete")
    public ResponseEntity<?> completeStudy() {
        // 학습 완료 처리 로직 (DB 업데이트 등)
        return ResponseEntity.ok().body("{\"status\":\"success\"}");
    }

    // 문법 20개 학습하기
    @GetMapping("grammar")
    public ResponseEntity<?> grammar(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        // 로그인된 유저 ID(int) 가져오기
        int userId = userDetails.getId();
        log.debug("로그인한 유저 아이디: " + userId);

        List<StudyEntity> studyContent = studyService.getRandomGrammarContentWithMistake("N4", "문법", userId);
        log.debug("========" + studyContent.toString());

        return ResponseEntity.ok().body(studyContent);
    }

    // 오답 노트 가져오기
    @GetMapping("mistakes")
    public ResponseEntity<?> mistakes(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        // 로그인된 유저 ID(int) 가져오기
        int userId = userDetails.getId();

        List<StudyEntity> userMistakes = studyService.mistakes(userId);
        return ResponseEntity.ok().body(userMistakes);
    }

    // 오답 노트 2 가져오기
    @GetMapping("mistakes2")
    public ResponseEntity<?> mistakes2(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        // 로그인된 유저 ID(int) 가져오기
        int userId = userDetails.getId();

        List<StudyEntity> userMistakes = studyService.mistakes2(userId);
        return ResponseEntity.ok().body(userMistakes);
    }
}
