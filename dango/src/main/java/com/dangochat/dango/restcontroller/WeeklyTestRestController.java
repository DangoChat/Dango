package com.dangochat.dango.restcontroller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import com.dangochat.dango.service.DailyQuizService;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/quiz/weekly")  // REST API용으로 경로를 /api로 변경
@RequiredArgsConstructor
@Slf4j
public class WeeklyTestRestController {
    private final GPTService gptService;
    private final DailyQuizService dailyQuizService;
    private final StudyService studyService;
    private final MemberRepository memberRepository;

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startDailyTest(@RequestBody Map<String, Integer> payload) {
        int userId = payload.get("userId");

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        String userNationality = member.getUserNationality();
        log.info("사용자 ID: {}, 국적: {}", userId, userNationality);

        List<String> studyWordContent = studyService.getWeekWordContent(userId); // 유저 ID를 이용해 학습 내용 가져오기
        // List<String> studyGrammerContent = studyService.getWeekGrammarContent(userId); // '문법' 타입 콘텐츠만 가져온다
        // 두 리스트를 합치기 위한 새로운 리스트 생성
        List<String> studyContent = new ArrayList<>();

        // 두 리스트를 studyContent에 추가
        studyContent.addAll(studyWordContent);
        // studyContent.addAll(studyGrammerContent);
        Collections.shuffle(studyContent);
        log.debug("daily shuffled : {}", studyContent);
        List<String> initialQuestions = generateWeeklyQuestions(studyContent, userNationality, 0,3);
        log.info("초기 3개의 Daily 문제 생성 완료: {}", initialQuestions);

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
        List<String> newQuestions = generateWeeklyQuestions(studyContent, userNationality, currentIndex, 1);
        generatedQuestions.addAll(newQuestions);
        
        return ResponseEntity.ok(createResponse(generatedQuestions));
    }

    private List<String> generateWeeklyQuestions(List<String> studyContent, String userNationality, int currentIndex, int count) {
        List<String> newQuestions = new ArrayList<>();

        try {
            int toIndex = Math.min(currentIndex + count, studyContent.size());
            if(toIndex > currentIndex){
                newQuestions = dailyQuizService.GenerateGPTQuestions(studyContent.subList(currentIndex, toIndex), 1, count, userNationality);
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
