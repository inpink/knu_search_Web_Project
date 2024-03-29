package knusearch.clear;

import knusearch.clear.survey.SurveyQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClearApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClearApplication.class, args);

    }

}
