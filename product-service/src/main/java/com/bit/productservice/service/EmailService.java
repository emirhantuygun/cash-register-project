package com.bit.productservice.service;

import com.bit.productservice.exception.EmailSendingFailedException;
import com.bit.productservice.exception.InvalidEmailFormatException;
import com.bit.productservice.exception.MissingEmailConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.regex.Pattern;

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
    public void validateEmailConfiguration() {
        if (fromEmailId == null || fromEmailId.isEmpty() || recipient == null || recipient.isEmpty()) {
            throw new MissingEmailConfigurationException("Email configuration is missing or incomplete.");
        }

        if (isNotValidEmail(fromEmailId)) {
            throw new InvalidEmailFormatException("From email address format is invalid: " + fromEmailId);
        }

        if (isNotValidEmail(recipient)) {
            throw new InvalidEmailFormatException("Recipient email address format is invalid: " + recipient);
        }
    }

    public void sendEmail(String subject, String body) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom(fromEmailId);
            simpleMailMessage.setTo(recipient);
            simpleMailMessage.setText(body);
            simpleMailMessage.setSubject(subject);

            javaMailSender.send(simpleMailMessage);
        } catch (MailException e) {
            throw new EmailSendingFailedException("Failed to send email: " + e.getMessage());
        }
    }

    private boolean isNotValidEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return !(pattern.matcher(email).matches());
    }
}

