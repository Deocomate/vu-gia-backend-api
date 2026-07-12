package vn.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {

    public static void main(String[] args) {
        // Đặt múi giờ mặc định của JVM là giờ Việt Nam (GMT+7) để log, LocalDateTime
        // và mọi xử lý thời gian phía server nhất quán với Asia/Ho_Chi_Minh.
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SpringApplication.run(Application.class, args);
    }

}
