package com.bit.productservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bit.productservice.exception.EmailSendingFailedException;
import com.bit.productservice.exception.InvalidEmailFormatException;
import com.bit.productservice.exception.MissingEmailConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setup() {
        String fromEmailId = "test@example.com";
        String recipient = "recipient@example.com";
        ReflectionTestUtils.setField(emailService, "fromEmailId", fromEmailId);
        ReflectionTestUtils.setField(emailService, "recipient", recipient);
    }

    @Test
    void shouldThrowMissingEmailConfigurationException_whenFromEmailIdIsNull() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "fromEmailId", null);

        // Act & Assert
        MissingEmailConfigurationException exception = assertThrows(
            MissingEmailConfigurationException.class,
            emailService::validateEmailConfiguration
        );

        assertEquals("Email configuration is missing or incomplete.", exception.getMessage());
    }

    @Test
    void shouldThrowMissingEmailConfigurationException_whenRecipientIsNull() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "recipient", null);

        // Act & Assert
        MissingEmailConfigurationException exception = assertThrows(
            MissingEmailConfigurationException.class,
            emailService::validateEmailConfiguration
        );

        assertEquals("Email configuration is missing or incomplete.", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidEmailFormatException_whenFromEmailIdIsInvalid() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "fromEmailId", "invalid-email");

        // Act & Assert
        InvalidEmailFormatException exception = assertThrows(
            InvalidEmailFormatException.class,
            emailService::validateEmailConfiguration
        );

        assertEquals("From email address format is invalid: invalid-email", exception.getMessage());
    }

    @Test
    void shouldThrowInvalidEmailFormatException_whenRecipientIsInvalid() {
        // Arrange
        ReflectionTestUtils.setField(emailService, "recipient", "invalid-email");

        // Act & Assert
        InvalidEmailFormatException exception = assertThrows(
            InvalidEmailFormatException.class,
            emailService::validateEmailConfiguration
        );

        assertEquals("Recipient email address format is invalid: invalid-email", exception.getMessage());
    }

    @Test
    void shouldNotThrowException_whenEmailConfigurationIsValid() {
        // Act & Assert
        assertDoesNotThrow(emailService::validateEmailConfiguration);
    }

    @Test
    void shouldSendEmailSuccessfully_whenEmailDetailsAreValid() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail(subject, body));

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldThrowEmailSendingFailedException_whenMailExceptionIsThrown() {
        // Arrange
        String subject = "Test Subject";
        String body = "Test Body";

        doThrow(new MailException("Mail server not available") {}).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        EmailSendingFailedException exception = assertThrows(
            EmailSendingFailedException.class,
            () -> emailService.sendEmail(subject, body)
        );

        assertEquals("Failed to send email: Mail server not available", exception.getMessage());
    }
}
