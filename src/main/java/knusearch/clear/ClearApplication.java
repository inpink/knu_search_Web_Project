package knusearch.clear;

import knusearch.clear.jpa.domain.BM25;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class ClearApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(ClearApplication.class, args);
    }
}
