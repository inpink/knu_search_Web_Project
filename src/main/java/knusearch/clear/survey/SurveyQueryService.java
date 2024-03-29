package knusearch.clear.survey;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyQueryService {

    private final SurveyQueryRepository surveyQueryRepository;

    @Transactional
    public void loadQueriesFromFile(String resourcePath) {
        try {
            System.out.println("ㅎㅇ");
            InputStream resourceStream = new ClassPathResource(resourcePath).getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                List<SurveyQuery> queries = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    SurveyQuery query = new SurveyQuery();
                    query.setQuery(line);
                    queries.add(query);
                }
                surveyQueryRepository.saveAll(queries);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
