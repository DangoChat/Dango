package com.dangochat.dango.service;

import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserMistakesEntity;
import com.dangochat.dango.entity.UserStudyContentEntity;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.repository.UserMistakesRepository;
import com.dangochat.dango.repository.UserStudyContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final UserStudyContentRepository userStudyContentRepository;
    private final UserMistakesRepository userMistakesRepository;
    private final StudyRepository studyRepository;
    private static final int LIMIT = 20;
    private static final double MAX_MISTAKE_RATIO = 0.2; // 최대 20%

    // 사용자 ID와 레벨에 따라 학습 콘텐츠 20개 가져 오기 (오답노트 최대 20% 포함, 비율은 랜덤)
    public List<StudyEntity> getRandomStudyContentByLevel(String level, String type,int userId) {

        // 1. 0%에서 20% 사이의 랜덤 비율을 결정
        double randomMistakeRatio = Math.random() * MAX_MISTAKE_RATIO; // 0.0 ~ 0.2 사이의 랜덤 값
        int mistakeLimit = (int) (LIMIT * randomMistakeRatio); // 가져올 오답 콘텐츠 개수

        // 2. 오답 노트에서 아직 해결되지 않은 학습 콘텐츠 가져 오기 (최대 mistakeLimit 개수)
        List<StudyEntity> mistakeContent = studyRepository.findMistakesByUserId(userId, mistakeLimit);

        // 3. 나머지는 일반 학습 콘텐츠에서 랜덤하게 가져 오기
        int generalLimit = LIMIT - mistakeLimit;
        List<StudyEntity> generalContent = studyRepository.findRandomByLevel(level, generalLimit);

        // 4. 두 리스트를 합친다
        List<StudyEntity> combinedContent = new ArrayList<>();
        combinedContent.addAll(mistakeContent);
        combinedContent.addAll(generalContent);

        return combinedContent;
    }

    // 유저 공부 기록 저장 (O,X 버튼 클릭 시)
    public void recordStudyContent(int studyContentId, int userId, boolean isCorrect) {
        UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
        userStudyContent.setUserId(userId);
        userStudyContent.setStudyContentId(studyContentId);
        userStudyContent.setRecordIsCorrect(isCorrect);
        userStudyContentRepository.save(userStudyContent);
    }

    // 오답 노트에 저장 (X 버튼 클릭 시)
    public void recordMistake(int userId, int studyContentId) {
        UserMistakesEntity userMistakes = new UserMistakesEntity();
        userMistakes.setUserId(userId);
        userMistakes.setStudyContentId(studyContentId);
        userMistakes.setMistakeResolved(false);
        userMistakes.setMistakeCounting(1);  // 처음 추가 되었을 때 count 는 1
        userMistakesRepository.save(userMistakes);
    }
}
