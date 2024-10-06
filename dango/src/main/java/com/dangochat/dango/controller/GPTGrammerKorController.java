package com.dangochat.dango.controller;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTGrammerKorService;
import com.dangochat.dango.service.MemberService;
import com.dangochat.dango.service.StudyService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GPTGrammerKorController {

    private final GPTGrammerKorService gptGrammerKorService;
    private final StudyService studyService;
    private final MemberService memberService;

    @GetMapping("grammer/korgrammer")
    public String getKorGrammerQuestions(Model model, HttpSession session, @AuthenticationPrincipal AuthenticatedUser userDetails) {
        int userId = userDetails.getId();
        String currentLevel = memberService.getUserCurrentLevel(userId);
        session.setAttribute("level", currentLevel);
        log.info("user level: {}", currentLevel);

        // 승급 테스트용 문법 문제 6개를 가져옴
        List<StudyDTO> grammerKorList = studyService.getGrammerKorContent(currentLevel);

        // 문법 문제 리스트를 세션에 저장
        session.setAttribute("grammerList", grammerKorList);
        session.setAttribute("currentIndex", 1);
        session.setAttribute("messageType", 1);

        // 문법 문제 생성 및 전달
        List<String> questions = gptGrammerKorService.loadQuestions(session, 1, currentLevel);

        // 모델에 데이터를 추가하여 View에 전달
        model.addAttribute("questions", questions);
        model.addAttribute("currentLevel", currentLevel);

        return "QuizView/grammerKor";  // 올바른 View 이름 반환
    }
}

