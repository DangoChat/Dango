package com.dangochat.dango.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/study") // localhost8888/api/study +
@RequiredArgsConstructor
public class StudyController {

    @GetMapping("/word")
    public String studyRestWord() {
        return "StudyView/RestWord";
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
