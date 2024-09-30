package knusearch.clear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ClearApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(ClearApplication.class, args);
    }
}
