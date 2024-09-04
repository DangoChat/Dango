package com.dangochat.dango.service;

import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private static final int LIMIT = 20;

    // 레벨 2인 데이터 중에서 랜덤으로 20개만 추출
    public List<StudyEntity> getRandomStudyContentByLevel(String level) {
        return studyRepository.findRandomByLevel(level, LIMIT);
    }
}

