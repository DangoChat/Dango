package com.dangochat.dango.controller;

import com.dangochat.dango.dto.GPTResponse;
import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.GPTService;
import com.dangochat.dango.service.StudyService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;
    private final GPTService gptService;

    @GetMapping("/word")
    public String studyRestWord() {
        return "StudyView/restWord";
    }
    @GetMapping("/grammar")
    public String studyRestGrammar() {
        return "StudyView/restGrammar";
    }
    @GetMapping("/wordMistakes")
    public String studyRestWordMistakes() {
        return "StudyView/restWordMistakes";
    }
    @GetMapping("/grammarMistakes")
    public String studyRestGrammarMistakes() {
        return "StudyView/restGrammarMistakes";
    }

}
