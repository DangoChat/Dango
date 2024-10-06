package com.dangochat.dango.restcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/quiz/listening")  
@RequiredArgsConstructor
@Slf4j
public class ListeningTestRestController {

    private final MemberRepository memberRepository;
    private final StudyService studyService;
    private final GPTService gptService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startListeningTest(@RequestBody Map<String, Object> payload) {
        int userId = (Integer) payload.get("userId");
        String level = (String) payload.get("level");

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String userNationality = member.getUserNationality();
        log.info("사용자 ID: {}, 국적: {}", userId, userNationality);

        // 사용자의 오늘 학습 내용 가져오기
        List<String> studyContent = studyService.studyContentForToday(userId);

        // 초기 3개의 청해 문제를 생성
        log.info("초기 3개의 청해 문제 생성 시작.");
        List<String> initialQuestions = generateListeningQuestions(studyContent, userNationality, 0,3);
        log.info("초기 3개의 청해 문제 생성 완료: {}", initialQuestions);

        Map<String, Object> response = new HashMap<>();
        response.put("questions", initialQuestions);
        response.put("studyContent", studyContent);  // studyContent를 프론트에 전달

        return ResponseEntity.ok(response);
    }

    // 청해 퀴즈의 다음 문제 요청
    @PostMapping("/next")
    public ResponseEntity<Map<String, Object>> nextListeningQuestion(@RequestBody Map<String, Object> payload) {
        log.debug("payload : {}", payload);

        List<String> generatedQuestions = (List<String>) payload.get("generatedQuestions");
        List<String> studyContent = (List<String>) payload.get("studyContent");
        String userNationality = (String) payload.get("userNationality");
        int currentIndex = (Integer) payload.get("currentIndex");  // 현재 문제의 인덱스를 프론트에서 받아옴

        // currentIndex를 반영하여 다음 단어에서 문제 생성
        List<String> newQuestions = generateListeningQuestions(studyContent, userNationality, currentIndex, 1);
        generatedQuestions.addAll(newQuestions);

        return ResponseEntity.ok(createResponse(generatedQuestions));
    }


    // 청해 문제를 생성하는 메서드 수정
    private List<String> generateListeningQuestions(List<String> studyContent, String userNationality, int currentIndex, int count) {
        List<String> newQuestions = new ArrayList<>();

        try {
            // currentIndex부터 문제를 생성하도록 수정
            int toIndex = Math.min(currentIndex + count, studyContent.size());
            if (toIndex > currentIndex) {
                newQuestions = gptService.generateGPTQuestions(studyContent.subList(currentIndex, toIndex), 1, count, userNationality);
                log.info("{}개의 새로운 청해 문제 생성: {}", newQuestions.size(), newQuestions);
            } else {
                log.warn("학습 내용이 부족하여 청해 문제를 생성할 수 없습니다.");
            }
        } catch (IOException e) {
            log.error("청해 문제 생성 중 오류 발생: ", e);
        }

        return newQuestions;
    }

    private Map<String, Object> createResponse(List<String> questions) {
        Map<String, Object> response = new HashMap<>();
        response.put("questions", questions);
        return response;
    }
}
