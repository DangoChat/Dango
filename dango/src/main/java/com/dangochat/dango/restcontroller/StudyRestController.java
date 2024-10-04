package com.dangochat.dango.restcontroller;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/study") // localhost8888/api/study +
@RequiredArgsConstructor
public class StudyRestController {

    private final StudyService studyService;

    @PostMapping("word")
    public List<StudyDTO> getWords(@RequestBody Map<String, Object> payload) {
        String level = (String) payload.get("level"); // jlptLevel 추출
        int userId = (Integer) payload.get("userId"); // userId 추출
        String type = (String) payload.get("type");
        log.debug("로그인한 유저 아이디: {}, type : {}, level : {}" ,userId, type, level);

        // String userlevel = studyService.getUserLevel(userId);  // 사용자 레벨 가져오기

        // StudyEntity를 StudyDTO로 변환하여 리스트로 저장
        // List<StudyDTO> studyContent = studyService.getRandomStudyContentByLevelAndType(userlevel, "단어", userId)
        List<StudyDTO> studyContent = studyService.getRandomStudyContentByLevelAndType(level, type, userId)
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
        System.out.println("studyContent constnet : " + studyContent);
        return studyContent;
    }

    // O, X를 누를 때 유저 공부 기록으로 보내는 기능.
    @PostMapping("/answer")
    public ResponseEntity<String> answer(@RequestBody Map<String, Object> payload,
                                         @AuthenticationPrincipal AuthenticatedUser userDetails) {
        try {
            // 숫자로 변환하여 처리
            int studyContentId = Integer.parseInt(payload.get("studyContentId").toString());
            String answer = (String) payload.get("answer");
            String studyType = (String) payload.get("studyType");
            int userId = userDetails.getId();  // @AuthenticationPrincipal을 통해 userId 가져오기
            boolean isCorrect = "O".equals(answer);
            // 학습 내용 기록
            studyService.recordStudyContent(studyContentId, userId, isCorrect, studyType);

            // 오답일 경우 오답 노트에 기록
            if (!isCorrect) {
                studyService.recordMistake(userId, studyContentId);
            }

            return ResponseEntity.ok("정답이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("정답 저장 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("정답 저장 중 오류 발생");
        }
    }

    // 문법 20개 학습하기
    @PostMapping("/grammar")
    public List<StudyDTO> studyGrammar(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        log.debug("로그인한 유저 아이디: " + userId);

        String level = studyService.getUserLevel(userId);

        // 문법 학습 콘텐츠 가져오기 및 DTO 변환
        List<StudyDTO> studyContent = studyService.getRandomGrammarContentWithMistake(level, "문법", userId)
                .stream()
                .map(studyEntity -> StudyDTO.builder()
                        .studyContentId(studyEntity.getStudyContentId())
                        .content(studyEntity.getContent())
                        .meaning(studyEntity.getMeaning())
                        .level(studyEntity.getLevel())
                        .example1(studyEntity.getExample1())
                        .exampleTranslation1(studyEntity.getExampleTranslation1())
                        .example2(studyEntity.getExample2())
                        .exampleTranslation2(studyEntity.getExampleTranslation2())
                        .build())
                .collect(Collectors.toList());

        return studyContent;
    }

    // 오답 노트 가져오기
    @GetMapping("/wordMistakes")
    public List<StudyDTO> getWordMistakes(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        log.debug("로그인한 유저 아이디: " + userId);

        // 오답 노트 리스트 가져오기 및 DTO 변환
        List<StudyDTO> userWordMistakes = studyService.wordMistakes(userId)
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

        return userWordMistakes;
    }
    // 오답 노트 2 가져오기
    @GetMapping("/grammarMistakes")
    public List<StudyDTO> getMistakes2(@AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        log.debug("로그인한 유저 아이디: " + userId);

        // 오답 노트 2 리스트 가져오기 및 DTO 변환
        List<StudyDTO> userMistakes = studyService.grammarMistakes(userId)
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

        return userMistakes;
    }




}
