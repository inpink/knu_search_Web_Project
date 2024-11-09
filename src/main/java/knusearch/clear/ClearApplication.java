package knusearch.clear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClearApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(ClearApplication.class, args);
    }
}
