package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.EmailDTO;
import catholic.ac.kr.secureuserapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("email")
public class EmailController {
    private final EmailService emailService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDTO request) {
        emailService.sendSimpleMail(request.getTo(), request.getSubject(), request.getBody());

        return ResponseEntity.ok("Email đã được gửi thành công!");
    }
}
