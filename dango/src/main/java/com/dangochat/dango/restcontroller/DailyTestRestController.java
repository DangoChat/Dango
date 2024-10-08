package com.dangochat.dango.restcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dangochat.dango.service.DailyQuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.entity.MemberEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/quiz/daily")  // REST API용으로 경로를 /api로 변경
@RequiredArgsConstructor
@Slf4j
public class DailyTestRestController {
    
    private final GPTService gptService;
    private final StudyService studyService;
    private final MemberRepository memberRepository;
    private final DailyQuizService dailyQuizService;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startDailyTest(@RequestBody Map<String, Integer> payload) {
        int userId = payload.get("userId");

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String userNationality = member.getUserNationality();
        log.info("사용자 ID: {}, 국적: {}", userId, userNationality);

        // 사용자의 오늘 학습 내용 가져오기
        List<String> studyContent = studyService.studyContentForToday(userId);
        Collections.shuffle(studyContent);
        log.debug("daily shuffled : {}", studyContent);
        List<String> initialQuestions = GenerateDailyQuestions(studyContent, userNationality, 0,3);
        log.info("초기 3개의 Daily 문제 생성 완료: {}", initialQuestions);

        // 문제를 데이터베이스에 저장
        dailyQuizService.saveQuestionsToDatabase(initialQuestions, member);

        Map<String, Object> response = new HashMap<>();
        response.put("questions", initialQuestions);
        response.put("studyContent", studyContent);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/next")
    public ResponseEntity<Map<String, Object>> nextDailyTest(@RequestBody Map<String, Object> payload) {
        List<String> generatedQuestions = (List<String>) payload.get("generatedQuestions");
        List<String> studyContent = (List<String>) payload.get("studyContent");
        String userNationality = (String) payload.get("userNationality");
        int currentIndex = (Integer) payload.get("currentIndex");  // 현재 문제의 인덱스를 프론트에서 받아옴
        
        // currentIndex를 반영하여 다음 단어에서 문제 생성
        List<String> newQuestions = GenerateDailyQuestions(studyContent, userNationality, currentIndex, 1);
        generatedQuestions.addAll(newQuestions);

        // 생성된 문제들을 데이터베이스에 저장
        MemberEntity member = memberRepository.findById((Integer) payload.get("userId"))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        dailyQuizService.saveQuestionsToDatabase(newQuestions, member);

        return ResponseEntity.ok(createResponse(generatedQuestions));
    }
    
    private List<String> GenerateDailyQuestions(List<String> studyContent, String userNationality, int currentIndex, int count) {
        List<String> newQuestions = new ArrayList<>();

        try {
            int toIndex = Math.min(currentIndex + count, studyContent.size());
            if(toIndex > currentIndex){
                newQuestions = dailyQuizService.GenerateGPTQuestions(studyContent.subList(currentIndex, toIndex), 2, count, userNationality);
            } else {
                log.debug("학습 내용 끝");
            }
        }catch(IOException e) {
            log.error("Daily Question Generating : ", e);
        }
        return newQuestions;
    }

    private Map<String, Object> createResponse(List<String> questions) {
        Map<String, Object> response = new HashMap<>();
        response.put("questions", questions);
        return response;
    }
}
