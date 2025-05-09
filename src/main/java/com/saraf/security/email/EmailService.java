package com.saraf.security.email;

import com.saraf.service.transfer.Status;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.SpringTemplateLoader;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.*;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplateName,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException, UnsupportedEncodingException {
        String templateName;
        if (emailTemplateName == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplateName.getName();
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        Context context = new Context();
        context.setVariables(properties);

        mimeMessageHelper.setFrom("amine@sarafbrk.com", "SARAF");
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);

        String template = templateEngine.process(templateName, context);

        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);

    }

    public void sendContactUsEmail(String name, String email, String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

        mimeMessageHelper.setFrom("amine@sarafbrk.com", "SARAF");
        mimeMessageHelper.setTo("contact@sarafbrk.com");
        mimeMessageHelper.setSubject("Contact Us Message");
        mimeMessageHelper.setText("You have received a new message from: \n\n" +
                                    "Name: " + name + "\n" +
                                    "Email: " + email + "\n" +
                                    "Message: " + message);

        mailSender.send(mimeMessage);
    }

    public void sendContactConfirmEmail(String name, String email, String message) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("message", message);

        Context context = new Context();
        context.setVariables(properties);

        mimeMessageHelper.setFrom("amine@sarafbrk.com", "SARAF");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Thank you for contacting us!");

        String template = templateEngine.process(EmailTemplateName.CONTACT_CONFIRM.name(), context);
        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }

    public void sendStatusUpdateEmail(String name, String email, Status status) throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("status", status.toString().toUpperCase());
        String message = status == Status.PROCESSING ? "We've received your payment and are now working on delivering the funds to your recipient in Algeria."
                       : status == Status.RECEIVED ? "The funds have been successfully delivered to your recipient in Algeria. They are now able to withdraw them."
                       : "Your transfer was canceled as we did not receive your payment. Please feel free to try again at your convenience.";
        properties.put("message", message);

        Context context = new Context();
        context.setVariables(properties);

        mimeMessageHelper.setFrom("amine@sarafbrk.com", "SARAF");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Transfer Status Update");

        String template = templateEngine.process(EmailTemplateName.STATUS_CHANGE.getName(), context);

        mimeMessageHelper.setText(template, true);

        mailSender.send(mimeMessage);
    }
}
