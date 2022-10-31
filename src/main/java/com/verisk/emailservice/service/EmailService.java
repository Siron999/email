package com.verisk.emailservice.service;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.verisk.emailservice.dto.CourseGradeReportDTO;
import com.verisk.emailservice.dto.ExamiationMarksheetDTO;
import com.verisk.emailservice.dto.SemesterEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;

    public Mono<Boolean> sendEmail(ExamiationMarksheetDTO examiationMarksheetDTO, List<ExamiationMarksheetDTO> examiationMarksheetDTOS) {
        return Mono.fromCallable(() -> {
            try {
                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
                helper.setTo(examiationMarksheetDTO.getEmail());
                helper.setFrom(from);
                helper.setSubject("Examination Grades Published");
                helper.setText(buildEmail(examiationMarksheetDTO), true);
                helper.addAttachment("marksheet.pdf", new ByteArrayResource(IOUtils.toByteArray(generatePDF(examiationMarksheetDTOS))), "application/pdf");
                javaMailSender.send(mimeMessage);
                return true;
            } catch (MessagingException e) {
                return false;
            }
        }).subscribeOn(Schedulers.parallel()).log();
    }

    private InputStream generatePDF(List<ExamiationMarksheetDTO> examiationMarksheetDTOs) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        Paragraph title = new Paragraph("Hello World How are you");
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
        PdfPTable table = new PdfPTable(examiationMarksheetDTOs.get(0).getCourseGrades().size() + 2);
        PdfPCell name = new PdfPCell(Phrase.getInstance("Name"));
        table.addCell(name);
        PdfPCell code = new PdfPCell(Phrase.getInstance("Student Code"));
        table.addCell(code);

        examiationMarksheetDTOs.get(0).getCourseGrades().forEach(x -> {
            PdfPCell course = new PdfPCell(Phrase.getInstance(x.getCourseName()));
            table.addCell(course);
        });

        examiationMarksheetDTOs.forEach(examiationMarksheetDTO -> {
            PdfPCell studentName = new PdfPCell(Phrase.getInstance(examiationMarksheetDTO.getFullName()));
            PdfPCell studentCode = new PdfPCell(Phrase.getInstance(examiationMarksheetDTO.getStudentCode()));
            table.addCell(studentName);
            table.addCell(studentCode);
            examiationMarksheetDTO.getCourseGrades().forEach(course -> {
                PdfPCell courseGrade = new PdfPCell(Phrase.getInstance(course.getResult().getGrade().toString()));
                table.addCell(courseGrade);
            });

        });
        document.add(table);
        document.close();
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        return in;
    }

    private String buildEmail(ExamiationMarksheetDTO examiationMarksheetDTO) {
        return """
                <div style="color:black;font-size:16px">Dear\040"""
                + examiationMarksheetDTO.getFullName() + ",<br>" + this.getExaminationName(examiationMarksheetDTO) +
                """
                        grades have been published.</span><br><br>"""
                + this.generateGradeTable(examiationMarksheetDTO.getCourseGrades()) + """
                <br><span style="color:black;">Thank you</span></div><br><br>""";

    }

    private String getExaminationName(ExamiationMarksheetDTO examiationMarksheetDTO) {
        String semesterName = "";
        if (examiationMarksheetDTO.getSemester().equals(SemesterEnum.SEMESTER_1)) {
            semesterName = "1st Semester";
        } else if (examiationMarksheetDTO.getSemester().equals(SemesterEnum.SEMESTER_2)) {
            semesterName = "2nd Semester";
        } else if (examiationMarksheetDTO.getSemester().equals(SemesterEnum.SEMESTER_3)) {
            semesterName = "3rd Semester";
        } else if (examiationMarksheetDTO.getSemester().equals(SemesterEnum.SEMESTER_4)) {
            semesterName = "4th Semester";
        }
        return String.format("%s %s %s ", examiationMarksheetDTO.getProgramName(), semesterName, examiationMarksheetDTO.getExamination());
    }

    private String generateGradeTable(List<CourseGradeReportDTO> courseGradeReportDTOs) {
        AtomicReference<String> table = new AtomicReference<>("""
                <table style="min-width:20vw;border-collapse:collapse;">
                    <tr style="border:1px solid black;">
                        <td style="border:1px solid black;padding:5px;">Course</td>
                        <td style="border:1px solid black;padding:5px;">Grade</td>
                    </tr>
                """);

        courseGradeReportDTOs.forEach(courseGradeReportDTO -> {
            table.set(table.get() +
                    """
                            <tr style="border:1px solid black">
                                <td style="border:1px solid black;padding:5px;">""" + courseGradeReportDTO.getCourseName() + """ 
                    </td>
                    <td style="border:1px solid black;padding:5px;">""" + courseGradeReportDTO.getResult().getGrade() + """
                            </td>
                            </tr>
                    """
            );
        });
        return table.get() + "</table>";
    }
}
