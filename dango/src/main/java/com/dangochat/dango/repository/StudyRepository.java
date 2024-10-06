package com.dangochat.dango.repository;

import com.dangochat.dango.entity.StudyEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;  // JpaRepository를 import
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRepository extends JpaRepository<StudyEntity, Integer> {

    // [ 학습하기 ]
    // Native Query로 레벨을 기준으로 학습 콘텐츠를 랜덤하게 가져오고, LIMIT을 적용
    @Query(value = "SELECT * FROM study_content WHERE level = :level AND study_content_type = :type ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<StudyEntity> findRandomByLevelAndType(@Param("level") String level, @Param("type") String type, @Param("limit") int limit);

    // Native Query로 특정 유저의 오답 노트(해결되지 않은)에서 studyContentId에 해당하는 StudyEntity를 랜덤하게 가져오고, LIMIT 적용
    @Query(value = "SELECT s.* FROM study_content s JOIN user_mistakes m ON s.study_content_id = m.study_content_id WHERE m.user_id = :userId AND m.mistake_resolved = false AND s.study_content_type = :type ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<StudyEntity> findMistakesByUserIdAndType(@Param("userId") int userId, @Param("type") String type, @Param("limit") int limit);

//    //[승급 테스트] - 한 단어 가져 와서 질문 하게 끔 하는 기능을 위해  study_content_content만 랜덤으로 가져오는 쿼리 단어 24개
//    @Query(value = "SELECT sc.study_content_content\n" +
//            "FROM study_content sc\n" +
//            "JOIN users u ON u.current_level = sc.level\n" +
//            "WHERE u.user_id = :userId\n" +  // Update with the correct user ID column name
//            "AND sc.study_content_type = '단어'\n" +
//            "AND sc.study_content_content REGEXP '[\\u4E00-\\u9FFF]'\n" +
//            "ORDER BY RAND() \n" +
//            "LIMIT 24;", nativeQuery = true)
//    List<String> findRandomContent(@Param("userId") int userId);

    // (미연) [ 승급 테스트 JLPT ]  단어 문제 -  level 똑같은 단어 24개 뽑기
    @Query(value = "SELECT se.study_content_content " +
            "FROM study_content se " +
            "WHERE se.level LIKE :level " +
            "AND study_content_type = '단어' " +
            "ORDER BY RAND() LIMIT 24", nativeQuery = true)
    List<String> findByJLPTWord(@Param("level") String level);

    // (미연) [ 승급 테스트 한국어 능력 시험 ] 단어 문제 - level 똑같은 단어 24개 뽑기
    @Query(value = "SELECT se.study_content_content " +
            "FROM study_content se" +
            " WHERE se.level LIKE :level" +
            " AND study_content_type = '단어'" +
            " AND se.study_content_content REGEXP '^[\uAC00-\uD7A3]+$' " +
            "AND se.study_content_content NOT LIKE '%다' " +
            "ORDER BY RAND() LIMIT 24", nativeQuery = true)
    List<String> findByKorWord(@Param("level") String level);

    // (성준) [ 승급 테스트 JLPT ] 문법 문제 - 단어 6개 뽑기
    @Query(value = "SELECT *\n" +
            "FROM study_content\n" +
            "WHERE level = :level \n" + // 사용자 level로 필터링
            "AND study_content_content NOT REGEXP '[\\u3040-\\u309F]' \n" + // 히라가나 제외
            "AND study_content_content NOT REGEXP '[\\u30A0-\\u30FF]' \n" + // 가타카나 제외
            "AND study_content_type = '문법'\n" +
            "ORDER BY RAND() \n" +
            "LIMIT 6;", nativeQuery = true)
    List<StudyEntity> findRandomGrammerContent(@Param("level")String level);

    // (미연) [ 승급 테스트 한국어 능력 시험 ] 문법 문제 단어 6개 뽑기
    @Query(value = "SELECT *\n" +
            "FROM study_content\n" +
            "WHERE level = :level \n" + // 사용자 level로 필터링
            "AND study_content_type = '문법' " +
            "ORDER BY RAND() LIMIT 6;", nativeQuery = true)
    List<StudyEntity> findRandomGrammerKorContent(@Param("level")String level);

    // 특정 유저의 '단어' 타입 학습 콘텐츠를 가져오는 쿼리 (일일 테스트)
    @Query("SELECT s FROM StudyEntity s JOIN UserStudyContentEntity usc ON s.studyContentId = usc.studyContent.studyContentId " +
    	       "WHERE usc.user.userId = :userId AND s.type = '단어' AND DATE(usc.recordStudyDate) = CURRENT_DATE")
    	List<StudyEntity> findTodayWordContentByUserId(@Param("userId") int userId);

 // 특정 유저의 '문법' 타입 학습 콘텐츠를 가져오는 쿼리  (일일테스트)
    @Query("SELECT s FROM StudyEntity s JOIN UserStudyContentEntity usc ON s.studyContentId = usc.studyContent.studyContentId " +
    	       "WHERE usc.user.userId = :userId AND s.type = '문법' AND DATE(usc.recordStudyDate) = CURRENT_DATE")
    	List<StudyEntity> findTodayGrammarContentByUserId(@Param("userId") int userId);

 // 특정 유저의 '단어' 타입 학습 콘텐츠를 가져오는 쿼리 (주간테스트)
    @Query("SELECT s FROM StudyEntity s JOIN UserStudyContentEntity usc ON s.studyContentId = usc.studyContent.studyContentId " +
    	       "WHERE usc.user.userId = :userId AND s.type = '단어' AND usc.recordStudyDate BETWEEN :startDate AND :endDate")
    	List<StudyEntity> findWeekWordContentByUserId(@Param("userId") int userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

 // 특정 유저의 '단어' 타입 학습 콘텐츠를 가져오는 쿼리 (주간테스트)
    @Query("SELECT s FROM StudyEntity s JOIN UserStudyContentEntity usc ON s.studyContentId = usc.studyContent.studyContentId " +
    	       "WHERE usc.user.userId = :userId AND s.type = '문법' AND usc.recordStudyDate BETWEEN :startDate AND :endDate")
    	List<StudyEntity> findWeekGrammarContentByUserId(@Param("userId") int userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);


}