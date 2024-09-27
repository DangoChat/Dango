package com.dangochat.dango.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dangochat.dango.dto.OMiKuZiDTO;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.OMiKuZiService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("OMiKuZi")
public class OMiKuZiController {

    private final OMiKuZiService omikuziService;

    // 오미쿠지 목록을 불러오는 메서드
    @GetMapping("OmikuziList")
    public String pullOMiKuZi(Model model, HttpSession session) {
        // Service를 통해 데이터베이스에서 오미쿠지 리스트를 가져옴
        List<OMiKuZiDTO> omikuziList = omikuziService.getAllOmikuzi();
        
        // 세션에서 선택된 오미쿠지가 있는지 확인
        OMiKuZiDTO selectedOmikuzi = (OMiKuZiDTO) session.getAttribute("selectedOmikuzi");
        
        // Model에 오미쿠지 리스트와 선택된 오미쿠지를 담아 View로 전달
        model.addAttribute("omikuziList", omikuziList);
        model.addAttribute("omikuzi", selectedOmikuzi);
        
        return "OMiKuZiView/pullOMiKuZi";
    }

    // 랜덤 오미쿠지 뽑기 및 세션 저장
    @PostMapping("OmikuziList")
    public String drawOmikuzi(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session, Model model) {
        // 랜덤 오미쿠지 뽑기
        OMiKuZiDTO selectedOmikuzi = omikuziService.drawRandomOmikuzi();

        // 세션에 저장
        session.setAttribute("selectedOmikuzi", selectedOmikuzi);
        
        // '大吉'이거나 "大凶"이면 마일리지 추가
        if ("大吉".equals(selectedOmikuzi.getOmikuziResult())) {
            omikuziService.addMileage(userDetails.getId(), 10);
        }else if("大凶".equals(selectedOmikuzi.getOmikuziResult())) {
        	omikuziService.addMileage(userDetails.getId(), 50);
        }

        // 뽑은 결과와 오미쿠지 리스트를 모델에 추가하여 페이지로 전달
        List<OMiKuZiDTO> omikuziList = omikuziService.getAllOmikuzi();
        model.addAttribute("omikuziList", omikuziList);
        model.addAttribute("omikuzi", selectedOmikuzi);
        
        // 결과를 pullOMiKuZi 페이지에서 표시
        return "OMiKuZiView/pullOMiKuZi";
    }
}
