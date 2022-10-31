package com.verisk.emailservice.consumer;

import com.verisk.emailservice.dto.ExamiationMarksheetDTO;
import com.verisk.emailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailConsumer {
    private final EmailService emailService;

    @RabbitListener(queues = "email_queue")
    public void consumeCreateStudentQueue(List<ExamiationMarksheetDTO> examiationMarksheetDTOs) {
        log.info("LMS send email message received");
        Flux.fromIterable(examiationMarksheetDTOs).flatMap(examiationMarksheetDTO -> emailService.sendEmail(examiationMarksheetDTO, examiationMarksheetDTOs)).subscribe((isSent) -> {
            if (isSent) {
                log.info("Email Sent");
            } else {
                log.error("Couldn't send email");
            }
        });
    }

}
