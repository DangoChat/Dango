package com.dangochat.dango.service;

import com.dangochat.dango.entity.StudyEntity;
import com.dangochat.dango.entity.UserMistakesEntity;
import com.dangochat.dango.entity.UserStudyContentEntity;
import com.dangochat.dango.repository.MemberRepository;
import com.dangochat.dango.repository.StudyRepository;
import com.dangochat.dango.repository.UserMistakesRepository;
import com.dangochat.dango.repository.UserStudyContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final UserStudyContentRepository userStudyContentRepository;
    private final UserMistakesRepository userMistakesRepository;
    private final MemberRepository memberRepository;

    private static final int LIMIT = 20;

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
    }
}
