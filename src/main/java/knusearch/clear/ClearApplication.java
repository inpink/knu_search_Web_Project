package knusearch.clear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableRetry
public class ClearApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(ClearApplication.class, args);
    }
}
