package com.dangochat.dango.controller;


import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("study")
public class StudyController {

    private final StudyService studyService;

    @GetMapping("word")
    public String studyword(Model model) {
        // 레벨 2인 데이터 중에서 랜덤으로 20개만 가져옴
        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevel("2");
        model.addAttribute("studyContent", studyContent);
        return "StudyView/word";
    }
}
