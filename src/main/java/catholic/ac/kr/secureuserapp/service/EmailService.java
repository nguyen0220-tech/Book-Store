package catholic.ac.kr.secureuserapp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

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
            //MimeMessage và MimeMessageHelper → Cho phép gửi email phức tạp (HTML, file, đa phần MIME)
            MimeMessage message = mailSender.createMimeMessage(); // Tạo đối tượng MimeMessage
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // Dùng helper hỗ trợ nội dung HTML
            // true = cho phép định dạng HTML và đính kèm
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);//"true" -> cho phép nội dung HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email HTML thất bại " + e);
        }
    }

    public void sendAttachmentsMail(String to, String subject, String body, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            FileSystemResource file = new FileSystemResource(new File(filePath));

            helper.addAttachment(file.getFilename(), file);
        } catch (MessagingException e) {
            throw new RuntimeException("Gửi email thất bại "+e);
        }
    }
}
