package com.bank.api.service.otp;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sendgrid.*;

import java.io.IOException;

@Service
public class OtpNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(OtpNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public OtpNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends OTP via Email with SMTP fallback to SendGrid.
     */
    public void sendOtpEmail(String email, String otp) {
        try {
            // ✅ First try SMTP (Gmail, Outlook, etc.)
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(email);
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: " + otp + ". It expires in 5 minutes.");
            mailSender.send(message);
            logger.info("OTP sent via SMTP to email: {}", email);
        } catch (Exception e) {
            logger.error("SMTP failed, switching to SendGrid. Reason: {}", e.getMessage());
            sendOtpWithSendGrid(email, otp);
        }
    }

    /**
     * Sends OTP via SendGrid API as a fallback.
     */
    private void sendOtpWithSendGrid(String email, String otp) {
        Email from = new Email("your-email@gmail.com");
        Email to = new Email(email);
        Content content = new Content("text/plain", "Your OTP code is: " + otp);
        Mail mail = new Mail(from, "Your OTP Code", to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("OTP sent successfully via SendGrid to: {}", email);
            } else {
                logger.error("SendGrid email failed with status: {} - {}", response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            logger.error("SendGrid email sending failed: {}", ex.getMessage());
        }
    }
}