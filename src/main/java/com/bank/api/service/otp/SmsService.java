package com.bank.api.service.otp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsService
{
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromPhoneNumber;

    public SmsService()
    {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String toPhoneNumber, String messageBody)
    {
        try
        {
            Message message = Message.creator(
                    new com.twilio.type.PhoneNumber(toPhoneNumber),
                    new com.twilio.type.PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            logger.info("SMS sent successfully to {}. Message SID: {}", toPhoneNumber, message.getSid());
        }
        catch (Exception e)
        {
            logger.error("Failed to send SMS to {}. Reason: {}", toPhoneNumber, e.getMessage());
        }
    }
}