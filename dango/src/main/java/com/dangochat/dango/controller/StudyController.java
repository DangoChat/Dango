package com.dangochat.dango.controller;


import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public String studyword(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // AuthenticatedUser로 캐스팅하여 userId(int)를 가져옴
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) userDetails;
        int userId = authenticatedUser.getId(); // 로그인된 유저 ID(int) 가져오기

        List<StudyEntity> studyContent = studyService.getRandomStudyContentByLevel("2", userId);
        model.addAttribute("studyContent", studyContent);
        return "StudyView/word";
    }

}
