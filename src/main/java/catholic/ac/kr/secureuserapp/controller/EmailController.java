package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.EmailRequest;
import catholic.ac.kr.secureuserapp.model.dto.HtmlEmailRequest;
import catholic.ac.kr.secureuserapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("email")
public class EmailController {
    private final EmailService emailService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendSimpleMail(request.getTo(), request.getSubject(), request.getBody());

        return ResponseEntity.ok("Email đã được gửi thành công!");
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("send-html")
    public ResponseEntity<String> sendHtmlEmail(@RequestBody HtmlEmailRequest request) {
        emailService.sendHtmlMail(request.getTo(), request.getSubject(), request.getBodyHtml());

        return ResponseEntity.ok("Email đã được gửi thành công!");
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("send-attachment")
    public ResponseEntity<String> sendAttachmentEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body,
            @RequestParam("file") MultipartFile file
            ){
        emailService.sendAttachmentsMail(to,subject,body,file);
        return ResponseEntity.ok("Email có file đính kèm đã được gửi!");
    }
}
