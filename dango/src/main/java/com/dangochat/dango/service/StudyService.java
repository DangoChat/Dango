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

    // 유저 공부 기록 저장 (O, X 상관없이)
    public void recordStudyContent(int studyContentId, String userId, boolean isCorrect) {
        UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
        userStudyContent.setUserId(userId);
        userStudyContent.setStudyContentId(studyContentId);
        userStudyContent.setRecordIsCorrect(isCorrect);  // isCorrect로 수정
        userStudyContentRepository.save(userStudyContent);
    }

<<<<<<< HEAD
    // 오답 노트에 저장 (X 버튼을 눌렀을 때만)
    public void recordMistake(String userId, int studyContentId) {
        UserMistakesEntity userMistakes = new UserMistakesEntity();
        userMistakes.setUserId(userId);
        userMistakes.setStudyContentId(studyContentId);
        userMistakes.setMistakeResolved(false);
        userMistakes.setMistakeCounting(1);  // 처음 추가되었을 때 count는 1
        userMistakesRepository.save(userMistakes);
    }
    // 사용자 ID와 레벨에 따라 학습 콘텐츠를 가져오는 메서드
    public List<StudyEntity> getRandomStudyContentByLevel(String level, String userId) {
        return studyRepository.findRandomByLevelAndUserId(level, userId);  // 사용자 ID와 레벨로 필터링
=======
    public List<StudyEntity> getRandomStudyContentByLevel(String level, int userId) {
        // userId가 0 이하일 경우 예외 처리
        if (userId <= 0 || !memberRepository.existsById(userId)) {
            throw new IllegalArgumentException("Invalid or missing user ID: " + userId);
        }

        // level에 따른 랜덤한 학습 콘텐츠 가져오기
        List<StudyEntity> studyContent = studyRepository.findRandomByLevel(level, LIMIT);

        for (StudyEntity content : studyContent) {
            UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
            userStudyContent.setStudyContentId(content.getStudyContentId());
            userStudyContent.setUserId(userId);
            userStudyContent.setRecordIsCorrect(true);  // 기본적으로 학습한 것으로 기록

            // 유저 공부 기록에 추가
            userStudyContentRepository.save(userStudyContent);

            // 틀린 경우 오답 노트에 추가
            if (!userStudyContent.isRecordIsCorrect()) {
                UserMistakesEntity newMistake = new UserMistakesEntity();
                newMistake.setUserId(userId);
                newMistake.setStudyContentId(content.getStudyContentId());
                newMistake.setMistakeResolved(false);
                newMistake.setMistakeCounting(1);  // 첫 오답이므로 1로 설정

                userMistakesRepository.save(newMistake);
            }
        }

        return studyContent;
>>>>>>> 398bc1f5764d9eca08d739f363c0f49462749339
    }
}
