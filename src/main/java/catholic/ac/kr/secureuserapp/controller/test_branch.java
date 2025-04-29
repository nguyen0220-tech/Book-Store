package catholic.ac.kr.secureuserapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class test_branch {
    public String test_branch() {
        return "test_branch";
    }
}
