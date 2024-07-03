package com.bit.productservice.service;

import com.bit.productservice.exception.EmailSendingFailedException;
import com.bit.productservice.exception.InvalidEmailFormatException;
import com.bit.productservice.exception.MissingEmailConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.regex.Pattern;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmailId;

    @Value("${email-recipient}")
    private String recipient;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private final JavaMailSender javaMailSender;

    @PostConstruct
    protected void validateEmailConfiguration() {
        log.trace("Entering validateEmailConfiguration method in EmailService");

        if (fromEmailId == null || fromEmailId.isEmpty() || recipient == null || recipient.isEmpty()) {
            log.error("Email configuration is missing or incomplete. fromEmailId: {}, recipient: {}", fromEmailId, recipient);
            throw new MissingEmailConfigurationException("Email configuration is missing or incomplete.");
        }

        if (isNotValidEmail(fromEmailId)) {
            log.error("From email address format is invalid: {}", fromEmailId);
            throw new InvalidEmailFormatException("From email address format is invalid: " + fromEmailId);
        }

        if (isNotValidEmail(recipient)) {
            log.error("Recipient email address format is invalid: {}", recipient);
            throw new InvalidEmailFormatException("Recipient email address format is invalid: " + recipient);
        }
        log.debug("Email configuration is valid. fromEmailId: {}, recipient: {}", fromEmailId, recipient);

        log.trace("Exiting validateEmailConfiguration method in EmailService");
    }

    protected void sendEmail(String subject, String body) {
        log.trace("Entering sendEmail method in EmailService");

        log.debug("Preparing to send email. Subject: {}, Body: {}", subject, body);
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmailId);
            simpleMailMessage.setTo(recipient);
            simpleMailMessage.setText(body);
            simpleMailMessage.setSubject(subject);

            javaMailSender.send(simpleMailMessage);
            log.info("Email sent successfully. Subject: {}, From: {}, To: {}", subject, fromEmailId, recipient);

        } catch (MailException e) {
            log.error("Failed to send email. Subject: {}, From: {}, To: {}. Error: {}", subject, fromEmailId, recipient, e.getMessage());
            throw new EmailSendingFailedException("Failed to send email: " + e.getMessage());
        }

        log.trace("Exiting sendEmail method in EmailService");
    }

    private boolean isNotValidEmail(String email) {
        log.trace("Entering isNotValidEmail method in EmailService");
        Pattern pattern = Pattern.compile(EMAIL_REGEX);

        log.trace("Exiting isNotValidEmail method in EmailService");
        return !(pattern.matcher(email).matches());
    }
}

