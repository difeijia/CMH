package com.neuedu.cmh.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class EmailUtil {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    /**
     * 发送验证码邮件（双参数版）
     * @param toEmail 目标邮箱地址
     * @param code 6位验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("您的验证码");
            message.setText(String.format(
                    "验证码：%s，有效期5分钟。请勿泄露给他人。\n（如非本人操作请忽略）",
                    code
            ));
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
