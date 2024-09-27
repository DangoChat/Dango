package com.dangochat.dango.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dangochat.dango.dto.OMiKuZiDTO;
import com.dangochat.dango.security.AuthenticatedUser;
import com.dangochat.dango.service.OMiKuZiService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/OMiKuZi")  // REST API에서는 보통 /api를 사용
public class OMiKuZiRestController {

    private final OMiKuZiService omikuziService;

    // 오미쿠지 목록을 불러오는 메서드
    @GetMapping("OmikuziList")
    public Map<String, Object> getOmikuziList(HttpSession session) {
        // Service를 통해 데이터베이스에서 오미쿠지 리스트를 가져옴
    	List<OMiKuZiDTO> omikuziList = omikuziService.getAllOmikuzi();
        
        // 세션에서 선택된 오미쿠지가 있는지 확인
    	 OMiKuZiDTO selectedOmikuzi = (OMiKuZiDTO) session.getAttribute("selectedOmikuzi");
        
    	 Map<String, Object> response = new HashMap<>();
    	    response.put("omikuziList", omikuziList);
    	    response.put("selectedOmikuzi", selectedOmikuzi);  // 세션에서 선택된 오미쿠지를 함께 반환

    	    return response;
    }

    // 랜덤 오미쿠지 뽑기 및 세션 저장
    @PostMapping("OmikuziList")
    public OMiKuZiDTO drawOmikuzi(@AuthenticationPrincipal AuthenticatedUser userDetails, HttpSession session) {
        // 랜덤 오미쿠지 뽑기
        OMiKuZiDTO selectedOmikuzi = omikuziService.drawRandomOmikuzi();

        // 세션에 저장
        session.setAttribute("selectedOmikuzi", selectedOmikuzi);
        
        // '大吉'이면 마일리지 추가
        if ("大吉".equals(selectedOmikuzi.getOmikuziResult())) {
            omikuziService.addMileage(userDetails.getId(), 300);
        }
        
        // 뽑은 오미쿠지 정보를 반환
        return selectedOmikuzi;
    }
}
