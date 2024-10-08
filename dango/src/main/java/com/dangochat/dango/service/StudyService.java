package com.dangochat.dango.service;


import com.dangochat.dango.config.DateUtils;

import com.dangochat.dango.dto.StudyDTO;
import com.dangochat.dango.dto.UserStudyContentDTO;
import com.dangochat.dango.entity.*;
import com.dangochat.dango.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyService {

    private final UserStudyContentRepository userStudyContentRepository;
    private final UserMistakesRepository userMistakesRepository;
    private final MemberRepository memberRepository;
    private final StudyRepository studyRepository;
    private final UserQuizQuestionReviewRepository userQuizQuestionReviewRepository;
    private final UserCompletionRateRepository userCompletionRateRepository;

    private static final int LIMIT = 20;
    private static final double MAX_MISTAKE_RATIO = 0.2; // 최대 20%
    private static final int LIMIT2 = 3;

    //유저의 커린트레벨을 가저오는 메서드
    public String getUserLevel(int userId) {
        return memberRepository.findById(userId)
                .map(MemberEntity::getCurrentLevel)  // 유저의 레벨 정보
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다: " + userId));
    }
    //유저의 오리지날 레벨을 가저오는 메서드
    public String getOriginalLevel(int userId) {
        return memberRepository.findById(userId)
                .map(MemberEntity::getOriginalLevel)  // 유저의 originalLevel 정보 가져오기
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자 ID입니다: " + userId));
    }
    
    //오리지날 레벨과 커렌트 레벨을 비교하기 위해 필요한 메서드
    public boolean isInvalidHigherLevelChangeJP(String originalLevel, String currentLevel) {
        Map<String, Integer> levelMap = Map.of(
            "N1", 1,  // 가장 높은 레벨
            "N2", 2,
            "N3", 3,
            "N4", 4,
            "N5", 5   // 가장 낮은 레벨
        );
        // originalLevel이 currentLevel보다 높은 경우 true 반환 (즉, 레벨을 하향하면 true)
        return levelMap.get(originalLevel) < levelMap.get(currentLevel);
    }
    //오리지날 레벨과 커렌트 레벨을 비교하기 위해 필요한 메서드
    public boolean isInvalidHigherLevelChangeKR(String originalLevel, String currentLevel) {
    	
        Map<String, Integer> levelMap = Map.of(
            "6", 6,  // 가장 높은 레벨
            "5", 5,
            "4", 4,
            "3", 3,
            "2", 2,
            "1", 1// 가장 낮은 레벨
        );
        // originalLevel이 currentLevel보다 높은 경우 true 반환 (즉, 레벨을 하향하면 true)
        return levelMap.get(originalLevel) < levelMap.get(currentLevel);
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
        String userNationality = user.getUserNationality();
        
        UserStudyContentEntity userStudyContent = new UserStudyContentEntity();
        userStudyContent.setUser(user);
        userStudyContent.setStudyContent(studyContent);
        userStudyContent.setRecordIsCorrect(isCorrect);
        userStudyContentRepository.save(userStudyContent);

        // 레벨 비교 로직 추가
        String currentLevel = getUserLevel(userId);  // 현재 레벨
        String originalLevel = getOriginalLevel(userId);  // 오리지널 레벨
        
        // 만약 현재 레벨이 오리지널 레벨보다 낮으면 마일리지 지급 중단
        if ("Japan".equalsIgnoreCase(userNationality)) {
            // 만약 현재 레벨이 오리지널 레벨보다 낮으면 마일리지 지급 중단
            if (isInvalidHigherLevelChangeKR(originalLevel, currentLevel)) {
                log.info("현재 레벨이 original 레벨보다 낮아 마일리지가 지급되지 않습니다.");
                return;  // 마일리지 지급 중지
            }
        }else if ("Korea".equalsIgnoreCase(userNationality)) {
            // 만약 현재 레벨이 오리지널 레벨보다 낮으면 마일리지 지급 중단
            if (isInvalidHigherLevelChangeJP(originalLevel, currentLevel)) {
                log.info("현재 레벨이 original 레벨보다 낮아 마일리지가 지급되지 않습니다.");
                return;  // 마일리지 지급 중지
            }
        }
        
        // 마일리지 로직 (레벨 비교 후)
        if ("문법".equalsIgnoreCase(studyType)) {
            if (isCorrect) {
                // 문법 학습에서 정답일 경우 +3 마일리지
                user.setUserMileage(user.getUserMileage() + 3);
            } else {
                // 문법 학습에서 오답일 경우 +1 마일리지
                user.setUserMileage(user.getUserMileage() + 1);
            }
        } else if ("단어".equalsIgnoreCase(studyType) && isCorrect) {
            user.setUserMileage(user.getUserMileage() + 1);
        }

        // 변경된 마일리지 정보 저장
        memberRepository.save(user);
        
        // 달성 포인트 누적 로직   
        int achievementPoints = 0;
        if ("Korea".equalsIgnoreCase(userNationality) && isCorrect) {
            switch (originalLevel) {
                case "N1": achievementPoints = 12; break;
                case "N2": achievementPoints = 11; break;
                case "N3": achievementPoints = 6; break;
                case "N4": achievementPoints = 5; break;
                case "N5": achievementPoints = 2; break;
                default: log.info("유효하지 않은 레벨: {}", originalLevel); break;
            }
        } else if ("Japan".equalsIgnoreCase(userNationality) && isCorrect) {
            switch (originalLevel) {
                case "6": achievementPoints = 8; break;
                case "5": achievementPoints = 6; break;
                case "4": achievementPoints = 4; break;
                case "3": achievementPoints = 3; break;
                case "2": achievementPoints = 2; break;
                case "1": achievementPoints = 1; break;
                default: log.info("유효하지 않은 레벨: {}", originalLevel); break;
            }
        }

        // 포인트 누적
        if (achievementPoints > 0) {
            // 포인트를 누적한 적이 있다면 
            if(userCompletionRateRepository.findByUser_UserId(userId).isPresent()){
                userCompletionRateRepository.updatePoints(userId, achievementPoints);
                System.out.println("achievement update 되는 중..." + achievementPoints + "userId : ," + userId);
            }
            // 포인트를 처음으로 누적하는 것이라면 
            else{
                UserCompletionRateEntity entity = UserCompletionRateEntity.builder()
                    .user(user)
                    .weeklyPoints(achievementPoints)
                    .totalPoints(achievementPoints)
                    .completionDate(LocalDateTime.now())
                    .build();
                userCompletionRateRepository.save(entity);
                System.out.println("achievement save 되는 중..." + achievementPoints + "userId : ," + userId);
            }
            // // userCompletionRateRepository.updatePoints(userId, achievementPoints);
            // System.out.println("achievement : 되는 중..." + achievementPoints + "userId : ," + userId);
        }
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
    public List<StudyEntity> wordMistakes(int userId) {

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


    public List<StudyEntity> grammarMistakes(int userId) {

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


 // 유저의 오늘 공부 기록을 가져와서 청해 문제로 만드는 서비스 메서드
    @Transactional(readOnly = true)
    public List<String> studyContentForToday(int userId) {
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));

        // 오늘의 시작 시각 (00:00:00)과 끝 시각 (23:59:59)을 구하기
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return userStudyContentRepository.findByUserAndRecordStudyDateBetween(user, startOfToday, endOfToday).stream()
                .map(userStudyContentEntity -> userStudyContentEntity.getStudyContent().getContent())
                .collect(Collectors.toList());
    }


    // 공부 내용에서 승급 테스트시 사용할 문법만 6개 가져오기 JLPT
    public List<StudyDTO> getGrammerContent(String userLevel){
        List<StudyEntity> studyGrammerContent = studyRepository.findRandomGrammerContent(userLevel);
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


    // 한국어 능력시험 승급 테스트에서 사용할 문법 문제 6개를 가져오는 메서드
    public List<StudyDTO> getGrammerKorContent(String userLevel) {
        List<StudyEntity> studyGrammerContent = studyRepository.findRandomGrammerKorContent(userLevel);
        List<StudyDTO> studyDTOList = new ArrayList<>();

        for (StudyEntity entity : studyGrammerContent) {
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
    
    
    public List<UserQuizQuestionReviewEntity> findQuizByTypeAndUserId(QuizType quizType, int userId) {
        return userQuizQuestionReviewRepository.findByQuizTypeAndUserId(quizType, userId);
    }
    
    //사용자 공부기록 가져오기
    public List<String> getUserStudyDates(int userId) {
        return userStudyContentRepository.findStudyDatesByUserId(userId);
    }
    
    
    // 유저 단어,문법 학습한 내용 가저오는 메서드
    public List<UserStudyContentDTO> getUserWordStudyContentByDate(int userId, Date date) {
        return userStudyContentRepository.findStudyContentByUserIdAndDate(userId, date).stream()
        		.filter(entity -> "단어".equals(entity.getStudyContent().getType()))
        		.map(entity -> UserStudyContentDTO.builder()
                .userStudyRecordId(entity.getUserStudyRecordId())
                .studyContentId(entity.getStudyContent().getStudyContentId())
                .userId(entity.getUser().getUserId())
                .recordStudyDate(entity.getRecordStudyDate())
                .recordIsCorrect(entity.isRecordIsCorrect())
                .content(entity.getStudyContent().getContent())
                .pronunciation(entity.getStudyContent().getPronunciation())
                .meaning(entity.getStudyContent().getMeaning())
                .type(entity.getStudyContent().getType())
                .build() // 예문 관련 필드는 제거
            )
            .collect(Collectors.toList());
    }

    
    public List<UserStudyContentDTO> getUserGrammarStudyContentByDate(int userId, Date date) {
        return userStudyContentRepository.findStudyContentByUserIdAndDate(userId, date).stream()
                .filter(entity -> "문법".equals(entity.getStudyContent().getType())) // type이 '문법'인 정보만 필터링
                .map(entity -> UserStudyContentDTO.builder()
                .userStudyRecordId(entity.getUserStudyRecordId())
                .studyContentId(entity.getStudyContent().getStudyContentId())
                .userId(entity.getUser().getUserId())
                .recordStudyDate(entity.getRecordStudyDate())
                .recordIsCorrect(entity.isRecordIsCorrect())
                .content(entity.getStudyContent().getContent())
                .pronunciation(entity.getStudyContent().getPronunciation())
                .meaning(entity.getStudyContent().getMeaning())
                .type(entity.getStudyContent().getType())
                .example1(entity.getStudyContent().getExample1())                 // 추가된 필드
                .exampleTranslation1(entity.getStudyContent().getExampleTranslation1()) // 추가된 필드
                .example2(entity.getStudyContent().getExample2())                 // 추가된 필드
                .exampleTranslation2(entity.getStudyContent().getExampleTranslation2()) // 추가된 필드
                .build()
            )
            .collect(Collectors.toList());
    }
    
    
 
    
	
	
}
