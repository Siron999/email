package com.verisk.emailservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ExamiationMarksheetDTO {
    private String studentId;
    private String studentCode;
    private String email;
    private ExaminationEnum examination;
    private String fullName;
    private String programName;
    private SemesterEnum semester;
    private List<CourseGradeReportDTO> courseGrades = new ArrayList<>();
}
