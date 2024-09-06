package com.dangochat.dango.service;

import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserMistakesEntity;
import com.dangochat.dango.entity.UserStudyContentEntity;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.repository.UserMistakesRepository;
import com.dangochat.dango.repository.UserStudyContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final UserStudyContentRepository userStudyContentRepository;
    private final UserMistakesRepository userMistakesRepository;
    private final StudyRepository studyRepository;
    private static final int LIMIT = 20;

    // 유저 공부 기록 저장 (O,X 버튼 클릭 시)
    public void recordStudyContent(int studyContentId, int userId, boolean isCorrect) {
        UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
        userStudyContent.setUserId(userId);
        userStudyContent.setStudyContentId(studyContentId);
        userStudyContent.setRecordIsCorrect(isCorrect);
        userStudyContentRepository.save(userStudyContent);
    }

    // 오답 노트에도 저장 (X 버튼 클릭 시)
    public void recordMistake(int userId, int studyContentId) {
        UserMistakesEntity userMistakes = new UserMistakesEntity();
        userMistakes.setUserId(userId);
        userMistakes.setStudyContentId(studyContentId);
        userMistakes.setMistakeResolved(false);
        userMistakes.setMistakeCounting(1);  // 처음 추가되었을 때 count는 1
        userMistakesRepository.save(userMistakes);
    }
    // 사용자 ID와 레벨에 따라 학습 콘텐츠를 가져 오기
    public List<StudyEntity> getRandomStudyContentByLevel(String level, int userId) {

        // level에 따른 랜덤한 학습 콘텐츠 limit 개수 만큼 가져 오기
        List<StudyEntity> studyContent = studyRepository.findRandomByLevel(level, LIMIT);

        for (StudyEntity content : studyContent) {
            UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
            userStudyContent.setStudyContentId(content.getStudyContentId());
            userStudyContent.setUserId(userId);
            userStudyContent.setRecordIsCorrect(true);  // 기본적 으로 학습한 것으로 기록

            // o, x 클릭 > 유저 공부 기록에 추가
            userStudyContentRepository.save(userStudyContent);

            // X 클릭 >  오답 노트에도 추가
            if (!userStudyContent.isRecordIsCorrect()) {
                UserMistakesEntity newMistake = new UserMistakesEntity();
                newMistake.setUserId(userId);
                newMistake.setStudyContentId(content.getStudyContentId());
                newMistake.setMistakeResolved(false);
                newMistake.setMistakeCounting(1);  // 첫 오답 이므로 1로 설정

                userMistakesRepository.save(newMistake);
            }
        }

        return studyContent;
    }
}
