package com.verisk.emailservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CourseGradeReportDTO {
    private String courseName;
    private StudentResultReportDTO result;
}
