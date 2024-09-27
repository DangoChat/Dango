package com.dangochat.dango.service;


import com.dangochat.dango.config.DateUtils;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.entity.*;
import com.dangochat.dango.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {

    private final UserStudyContentRepository userStudyContentRepository;
    private final UserMistakesRepository userMistakesRepository;
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;

    private static final int LIMIT = 20;
    private static final double MAX_MISTAKE_RATIO = 0.2; // 최대 20%
    private static final int LIMIT2 = 3;

    public String getUserLevel(int userId) {
        return memberRepository.findById(userId)
                .map(MemberEntity::getCurrentLevel)  // 유저의 레벨 정보
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다: " + userId));
    }

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

    // 사용자 ID와 레벨에 따라 학습 콘텐츠 20개 가져 오기 (오답노트 최대 20% 포함, 비율은 랜덤)
    public List<StudyEntity> getRandomGrammarContentWithMistake(String level, String type, int userId) {

        int mistakeLimit = 1;  // 전날 틀린 문법 1개
        List<StudyEntity> mistakeContent = studyRepository.findMistakesByUserIdAndType(userId, type, mistakeLimit);  // limit 값 추가

        // 나머지 콘텐츠를 일반 학습 콘텐츠에서 랜덤하게 가져오기
        int generalLimit = LIMIT2 - mistakeContent.size();
        List<StudyEntity> generalContent = studyRepository.findRandomByLevelAndType(level, type, generalLimit);

        // 두 리스트를 합친다
        List<StudyEntity> combinedContent = new ArrayList<>();
        combinedContent.addAll(mistakeContent);
        combinedContent.addAll(generalContent);

        return combinedContent;
    }


    // 유저 공부 기록 저장 (O,X 버튼 클릭 시)
    public void recordStudyContent(int studyContentId, int userId, boolean isCorrect, String studyType) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        StudyEntity studyContent = studyRepository.findById(studyContentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid study content ID: " + studyContentId));

        UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
        userStudyContent.setUser(user);
        userStudyContent.setStudyContent(studyContent);
        userStudyContent.setRecordIsCorrect(isCorrect);
        userStudyContentRepository.save(userStudyContent);

        // 마일리지를 처리하는 로직 (MemberEntity의 userMileage 필드 직접 수정)
        if ("grammer".equalsIgnoreCase(studyType)) {
            if (isCorrect) {
                // 문법 학습에서 정답일 경우 +3 마일리지
                user.setUserMileage(user.getUserMileage() + 3);
            } else {
                // 문법 학습에서 오답일 경우 +1 마일리지
                user.setUserMileage(user.getUserMileage() + 1);
            }
        }
        else if("word".equalsIgnoreCase(studyType) && isCorrect) {
            user.setUserMileage(user.getUserMileage() + 1);
        }

        // 변경된 마일리지 정보 저장
        memberRepository.save(user);
    }

    // 오답 노트에 저장 (X 버튼 클릭 시)
    public void recordMistake(int userId, int studyContentId) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow();
        StudyEntity studyContent = studyRepository.findById(studyContentId)
                .orElseThrow();
        UserMistakesEntity userMistakes = new UserMistakesEntity();
        userMistakes.setUser(user);
        userMistakes.setStudyContent(studyContent);
        userMistakes.setMistakeResolved(false);

        userMistakes.setMistakeCounting(1);  // 처음 추가 되었을 때 count 는 1
        userMistakesRepository.save(userMistakes);
    }


    // 사용자의 오답과 관련된 학습 콘텐츠 반환
    public List<StudyEntity> mistakes(int userId) {

        // 사용자 정보를 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow();

        // 사용자의 오답 기록에서 관련된 StudyEntity 리스트 추출
        List<UserMistakesEntity> mistakes = userMistakesRepository.findByUser(user);

        // UserMistakesEntity에서 StudyEntity로 변환하여 필요한 필드들을 가져옴
        List<StudyEntity> userMistakes = mistakes.stream()
                .map(UserMistakesEntity::getStudyContent)
                .filter(studyContent -> "단어".equals(studyContent.getType()))
                .collect(Collectors.toList());

        return userMistakes;
    }


    public List<StudyEntity> mistakes2(int userId) {

        // 사용자 정보를 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow();

        // 사용자의 오답 기록에서 관련된 StudyEntity 리스트 추출
        List<UserMistakesEntity> mistakes = userMistakesRepository.findByUser(user);

        // UserMistakesEntity에서 StudyEntity로 변환하여 필요한 필드들을 가져옴
        List<StudyEntity> userMistakes = mistakes.stream()
                .map(UserMistakesEntity::getStudyContent)
                .filter(studyContent -> "문법".equals(studyContent.getType()))
                .collect(Collectors.toList());

        return userMistakes;
    }


    // 유저 공부기록 가져와서 청해문제를 gpt로 만든 후 HTML로 뿌려주는 컨트롤러
    @Transactional(readOnly = true)
    public List<String> studyContent(int userId) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        return userStudyContentRepository.findByUser(user).stream()
                .map(userStudyContentEntity -> userStudyContentEntity.getStudyContent().getContent())
                .collect(Collectors.toList());
    }

    // 공부 내용에서 승급 테스트시 사용할 문법만 6개 가져오기
    public List<StudyDTO> getGrammerContent(){
        List<StudyEntity> studyGrammerContent = studyRepository.findRandomGrammerContent();
        List<StudyDTO> studyDTOList = new ArrayList<>();

        for (StudyEntity entity : studyGrammerContent){
            StudyDTO dto = new StudyDTO(
                    entity.getStudyContentId(),
                    entity.getContent(),
                    entity.getPronunciation(),
                    entity.getMeaning(),
                    entity.getType(),
                    entity.getLevel(),
                    entity.getExample1(),
                    entity.getExampleTranslation1(),
                    entity.getExample2(),
                    entity.getExampleTranslation2()
            );
            studyDTOList.add(dto);
        }
        return studyDTOList;
    }



    // repository에 사용자가 하루간 학습한 내용 가저오는 쿼리 요청
    public List<String> getTodayWordContent(int userId) {
        List<StudyEntity> wordContentEntities = studyRepository.findTodayWordContentByUserId(userId);
        return wordContentEntities.stream()
                .map(StudyEntity::getContent)
                .toList();
    }

    // repository에 사용자가 하루간 학습한 내용 가저오는 쿼리 요청
    public List<String> getTodayGrammarContent(int userId) {
        List<StudyEntity> grammarContentEntities = studyRepository.findTodayGrammarContentByUserId(userId);
        return grammarContentEntities.stream()
                .map(StudyEntity::getContent)
                .toList();
    }


    // repository에 사용자가 주가동안 학습한 내용 가저오는 쿼리 요청
    public List<String> getWeekWordContent(int userId) {
        Date today = new Date();  // 오늘 날짜
        Date startOfWeek = DateUtils.getStartOfWeek(today);  // 해당 주의 월요일
        Date endOfWeek = DateUtils.getEndOfWeek(today);  // 해당 주의 일요일

        log.info("Start of week:{} " , startOfWeek);
        log.info("End of week: {}" ,endOfWeek);
        List<StudyEntity> wordContentEntities = studyRepository.findWeekWordContentByUserId(userId, startOfWeek, endOfWeek);
        return wordContentEntities.stream()
                .map(StudyEntity::getContent)
                .toList();
    }


    // repository에 사용자가 주가동안 학습한 내용 가저오는 쿼리 요청
    public List<String> getWeekGrammarContent(int userId) {
        Date today = new Date();  // 오늘 날짜
        Date startOfWeek = DateUtils.getStartOfWeek(today);  // 해당 주의 월요일
        Date endOfWeek = DateUtils.getEndOfWeek(today);  // 해당 주의 일요일

        log.info("Start of week:{} " , startOfWeek);
        log.info("End of week: {}" ,endOfWeek);
        List<StudyEntity> wordContentEntities = studyRepository.findWeekGrammarContentByUserId(userId, startOfWeek, endOfWeek);
        return wordContentEntities.stream()
                .map(StudyEntity::getContent)
                .toList();
    }



}
