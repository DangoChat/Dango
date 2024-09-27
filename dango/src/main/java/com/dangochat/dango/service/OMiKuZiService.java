package com.dangochat.dango.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dangochat.dango.dto.OMiKuZiDTO;
import com.dangochat.dango.entity.OMiKuZiEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.OMiKuZiRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class OMiKuZiService {

	 private final OMiKuZiRepository omikuziRepository;
	 private final MemberRepository memberRepository;
	
	 
	 // 모든 오미쿠지 데이터를 가져오는 메서드
	    public List<OMiKuZiDTO> getAllOmikuzi() {
	        List<OMiKuZiEntity> omikuziEntities = omikuziRepository.findAll();
	        return omikuziEntities.stream()
	            .map(this::convertToDto)
	            .collect(Collectors.toList());
	    }

	    // Entity를 DTO로 변환하는 메서드
	    private OMiKuZiDTO convertToDto(OMiKuZiEntity entity) {
	        return new OMiKuZiDTO(
	            entity.getOmikuziId(),
	            entity.getOmikuziResult(),
	            entity.getKrDescription(),  // 한국어 설명 추가
	            entity.getJpDescription()   // 일본어 설명 추가
	        );
	    }

	    // 랜덤 오미쿠지 뽑기
	    public OMiKuZiDTO drawRandomOmikuzi() {
	        List<OMiKuZiEntity> omikuziList = omikuziRepository.findAll();
	        Random random = new Random();
	        OMiKuZiEntity selectedOmikuzi = omikuziList.get(random.nextInt(omikuziList.size()));
	        return convertToDto(selectedOmikuzi);
	    }

	    // '大吉'일 경우 마일리지 추가
	    public void addMileage(Integer userId, int points) {
	    	memberRepository.addMileage(userId, points);  // 마일리지 추가 로직
	    }
	    
	    
		
		
}
