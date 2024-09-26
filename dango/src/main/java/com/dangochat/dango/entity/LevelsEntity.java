package com.dangochat.dango.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "levels")
public class LevelsEntity {

    @Id
    @Column(name = "level", length = 200, nullable = false)
    private String level; //

    @Column(name = "level_exam_type", length = 200, nullable = false)
    private String levelExamType; //

    @Column(name = "level_description", length = 2000)
    private String levelDescription;
}
