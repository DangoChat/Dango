package com.dangochat.dango.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "omikuzi")
public class OMiKuZiEntity {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "omikuzi_id")  // 명시적으로 컬럼 이름을 지정
	    private Integer omikuziId;

	    @Column(nullable = false, length = 50)
	    private String omikuziResult;

	    @Column(name = "KR_description", nullable = false, length = 2000)
	    private String krDescription;  // 한국어 결과 설명

	    @Column(name = "JP_description", nullable = false, length = 2000)
	    private String jpDescription;  // 일본어 결과 설명
}
