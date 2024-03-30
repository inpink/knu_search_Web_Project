package knusearch.clear.survey.repository;

import knusearch.clear.survey.model.SurveyQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQueryRepository extends JpaRepository<SurveyQuery, Long> {

    SurveyQuery findQueryById(int queryId);
}
