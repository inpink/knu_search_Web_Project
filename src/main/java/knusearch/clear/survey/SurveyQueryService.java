package knusearch.clear.survey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class SurveyQueryService {

    @Autowired
    private SurveyQueryRepository repository;

    @Transactional
    public void loadQueriesFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<SurveyQuery> queries = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                SurveyQuery query = new SurveyQuery();
                query.setQuery(line);
                queries.add(query);
            }
            repository.saveAll(queries);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
