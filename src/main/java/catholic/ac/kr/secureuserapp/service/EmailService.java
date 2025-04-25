package catholic.ac.kr.secureuserapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("teemee2020@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message); // Gửi email
    }

    public void sendHtmlMail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage(); // Tạo đối tượng MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Dùng helper hỗ trợ nội dung HTML

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody,true);//"true" -> cho phép nội dung HTML

            mailSender.send(message);
        }catch (MessagingException e){
            throw new RuntimeException("Gửi email HTML thất bại "+e);
        }
    }
}
