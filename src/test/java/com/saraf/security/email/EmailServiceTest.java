package com.saraf.security.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendEmail_Success_WithSpecificTemplate() throws MessagingException, IOException {
        // Setup test data
        String to = "test@example.com";
        String from = "amine@sarafbrk.com";
        String username = "TestUser";
        String confirmationUrl = "http://example.com/confirm";
        String activationCode = "123456";
        String subject = "Test Subject";
        EmailTemplateName emailTemplateName = EmailTemplateName.ACTIVATE_ACCOUNT;

        // Mock MimeMessage creation and content
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock the template engine
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("Processed Template");

        // Mock MimeMessage methods
        when(mimeMessage.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress(from)});
        when(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)).thenReturn(new InternetAddress[]{new InternetAddress(to)});
        when(mimeMessage.getSubject()).thenReturn(subject);
        when(mimeMessage.getContent()).thenReturn("Processed Template");

        // Call the sendEmail method
        emailService.sendEmail(to, username, emailTemplateName, confirmationUrl, activationCode, subject);

        // Capture the MimeMessage sent by the mail sender
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Access the captured MimeMessage to perform assertions
        MimeMessage sentMessage = messageCaptor.getValue();

        assertEquals(from, sentMessage.getFrom()[0].toString());
        assertEquals(to, sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals("Processed Template", sentMessage.getContent().toString().trim());
    }

    @Test
    void sendEmail_Success_WithDefaultTemplate() throws MessagingException, IOException {
        // Setup test data
        String to = "test@example.com";
        String from = "amine@sarafbrk.com";
        String username = "TestUser";
        String confirmationUrl = "http://example.com/confirm";
        String activationCode = "123456";
        String subject = "Test Subject";

        // Mock MimeMessage creation and content
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock the template engine
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("Processed Default Template");

        // Mock MimeMessage methods
        when(mimeMessage.getFrom()).thenReturn(new InternetAddress[]{new InternetAddress(from)});
        when(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)).thenReturn(new InternetAddress[]{new InternetAddress(to)});
        when(mimeMessage.getSubject()).thenReturn(subject);
        when(mimeMessage.getContent()).thenReturn("Processed Default Template");

        // Call the sendEmail method with null EmailTemplateName (which uses the default template)
        emailService.sendEmail(to, username, null, confirmationUrl, activationCode, subject);

        // Capture the MimeMessage sent by the mail sender
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Access the captured MimeMessage to perform assertions
        MimeMessage sentMessage = messageCaptor.getValue();

        assertEquals(from, sentMessage.getFrom()[0].toString());
        assertEquals(to, sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals("Processed Default Template", sentMessage.getContent().toString().trim());
    }

    @Test
    void sendContactUsEmail_Success() throws MessagingException, IOException {
        // Setup test data
        String name = "John Doe";
        String email = "john.doe@example.com";
        String message = "This is a test message.";

        // Mock MimeMessage creation
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Call the sendContactUsEmail method
        emailService.sendContactUsEmail(name, email, message);

        when(mimeMessage.getSubject()).thenReturn("Contact Us Message");
        when(mimeMessage.getContent()).thenReturn(
                "You have received a new message from: \n" +
                    "Name: John Doe\n" +
                    "Email: john.doe@example.com\n" +
                    "Message: This is a test message."
        );


        // Capture the MimeMessage sent by the mail sender
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Access the captured MimeMessage to perform assertions
        MimeMessage sentMessage = messageCaptor.getValue();

        MimeMessageHelper helper = new MimeMessageHelper(sentMessage, true);

        assertEquals("Contact Us Message", sentMessage.getSubject());
        assertEquals(
                "You have received a new message from: \n" +
                        "Name: John Doe\n" +
                        "Email: john.doe@example.com\n" +
                        "Message: This is a test message.",
                sentMessage.getContent().toString().trim()
        );
    }

    @Test
    void sendContactConfirmEmail_Success() throws MessagingException, IOException {
        // Setup test data
        String name = "John Doe";
        String email = "john.doe@example.com";
        String message = "Thank you for your message.";

        // Mock MimeMessage creation and content
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock the template engine for the CONTACT_CONFIRM template
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("Processed Contact Confirm Template");
        when(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)).thenReturn(new InternetAddress[]{new InternetAddress(email)});
        when(mimeMessage.getSubject()).thenReturn("Thank you for contacting us!");
        when(mimeMessage.getContent()).thenReturn("Processed Contact Confirm Template");

        // Call the sendContactConfirmEmail method
        emailService.sendContactConfirmEmail(name, email, message);

        // Capture the MimeMessage sent by the mail sender
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        // Access the captured MimeMessage to perform assertions
        MimeMessage sentMessage = messageCaptor.getValue();

        MimeMessageHelper helper = new MimeMessageHelper(sentMessage, true);

        assertEquals(email, sentMessage.getRecipients(MimeMessage.RecipientType.TO)[0].toString());
        assertEquals("Thank you for contacting us!", sentMessage.getSubject());
        assertEquals("Processed Contact Confirm Template", sentMessage.getContent().toString().trim());
    }
}
