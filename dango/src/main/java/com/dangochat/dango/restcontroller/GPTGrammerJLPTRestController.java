package com.dangochat.dango.restcontroller;

import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.MemberService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.service.GPTGrammerService;
import com.dangochat.dango.service.StudyService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")  // Rest API 경로를 명시
public class GPTGrammerJLPTRestController {

    private final GPTGrammerService gptGrammerService;
    private final StudyService studyService;
    private final MemberService memberService;

    @GetMapping("/sungjun")
    public List<String> sungjun(HttpSession session,int count, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        String currentLevel = memberService.getUserCurrentLevel(userId);
        session.setAttribute("level", currentLevel);
        log.info("user level: {}", currentLevel);

        // 1. 승급 테스트용 문제를 만드는 경우
        // 1-1. 승급테스트에서 다룰 단어를 추출
        List<StudyDTO> grammerList = studyService.getGrammerContent(currentLevel);
        session.setAttribute("grammerList", grammerList);
        session.setAttribute("currentIndex", 1);
        session.setAttribute("messageType", 1);

        return gptGrammerService.loadQuestions(session, count, currentLevel);

    }
}
