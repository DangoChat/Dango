package com.dangochat.dango.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.service.GPTGrammerService;
import com.dangochat.dango.service.StudyService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GPTGrammerController {
    
    private final GPTGrammerService gptGrammerService;
    private final StudyService studyService;

    @GetMapping("sungjun")
    public String sungjun(Model model, HttpSession session) {
        // 1. 승급 테스트용 문제를 만드는 경우
        // 1-1. 승급테스트에서 다룰 단어를 추출
        List<StudyDTO> grammerList = studyService.getGrammerContent();
        // log.debug("grammer content : {} ", grammerList);
        session.setAttribute("grammerList", grammerList);
        session.setAttribute("currentIndex", 1);
        session.setAttribute("messageType", 1);
        // 1-2. 추출한 단어로 문제 생성 및 전달
        gptGrammerService.loadQuestions(session, 1, 1);
        // 1-3. 생성한 문제중 첫번째 문제를 화면에 표시
        // 2. 문법 일일 / 주간 테스트 문제를 만드는 경우

        return "QuizView/grammer";
    }
    
    
}
