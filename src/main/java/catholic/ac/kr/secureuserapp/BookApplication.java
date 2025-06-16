package catholic.ac.kr.secureuserapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "catholic.ac.kr.secureuserapp")
@EnableCaching // bật để test Cache: spring sẽ luư cache vao RAM (thực tế sẽ cần cài theem phần mềm nhu Redis, Doker...)
public class BookApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }

}
