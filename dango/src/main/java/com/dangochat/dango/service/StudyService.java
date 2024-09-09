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
    public List<StudyEntity> getRandomStudyContentByLevelAndType(String level, String type, int userId) {
        // 0~20%의 오답 콘텐츠를 랜덤하게 가져오기
        int mistakeLimit = (int) (LIMIT * Math.random() * MAX_MISTAKE_RATIO);
        List<StudyEntity> mistakeContent = studyRepository.findMistakesByUserIdAndType(userId, type, mistakeLimit);  // limit 값 추가

        // 나머지 콘텐츠를 일반 학습 콘텐츠에서 랜덤하게 가져오기
        int generalLimit = LIMIT - mistakeLimit;
        List<StudyEntity> generalContent = studyRepository.findRandomByLevelAndType(level, type, generalLimit);

        // 두 리스트를 합친다
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
