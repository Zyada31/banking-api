package com.bank.api.service.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

import java.security.SecureRandom;

@Service
public class OtpService
{
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int MAX_OTP_ATTEMPTS = 5;

    private final Cache<String, String> otpCache;
    private final Cache<String, Integer> otpAttemptsCache;
    private final OtpNotificationService otpNotificationService;
    private final SmsService smsService;

    public OtpService(OtpNotificationService otpNotificationService, SmsService smsService)
    {
        this.otpNotificationService = otpNotificationService;
        this.smsService = smsService;
        this.otpCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // OTP expires in 5 minutes
                .maximumSize(10000)
                .build();

        this.otpAttemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();
    }

    /**
     * Generates a secure 6-digit OTP for a transaction.
     * @param transactionId The unique transaction identifier.
     * @return Generated OTP.
     */
    public String generateOtp(String transactionId)
    {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        otpCache.put(transactionId, otp);

        logger.info("Generated OTP for Transaction [{}]: ******{}", transactionId, otp.substring(4));
        return otp;
    }

    /**
     * Generates a secure 6-digit OTP for a transaction.
     */
    public void generateAndSendOtp(String transactionId, String email, String phoneNumber)
    {
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));

        try
        {
            otpNotificationService.sendOtpEmail(email, otp);
            smsService.sendSms(phoneNumber, "Your OTP is: " + otp);
            logger.info("OTP successfully sent to email [{}] and phone [{}]", email, phoneNumber);
        }
        catch (Exception e)
        {
            logger.error("Failed to send OTP. Email: {}, Phone: {}. Reason: {}", email, phoneNumber, e.getMessage());
        }
    }

    /**
     * Validates OTP with retry limit.
     */
    public boolean validateOtp(String transactionId, String otp)
    {
        String storedOtp = otpCache.getIfPresent(transactionId);

        if (storedOtp == null)
        {
            logger.warn("OTP validation failed for Transaction [{}]: OTP expired or not found.", transactionId);
            return false;
        }

        int attempts = otpAttemptsCache.get(transactionId, key -> 0);
        if (attempts >= MAX_OTP_ATTEMPTS)
        {
            logger.warn("Transaction [{}] is locked due to too many failed OTP attempts.", transactionId);
            return false;
        }

        boolean isValid = storedOtp.equals(otp.trim());

        if (!isValid)
        {
            otpAttemptsCache.put(transactionId, attempts + 1);
            logger.warn("Invalid OTP attempt [{}] for Transaction [{}]: Entered OTP [{}]", attempts + 1, transactionId, otp);
        }
        else
        {
            otpCache.invalidate(transactionId);
            otpAttemptsCache.invalidate(transactionId);
        }

        return isValid;
    }
}