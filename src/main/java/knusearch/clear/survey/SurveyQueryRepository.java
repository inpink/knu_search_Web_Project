package knusearch.clear.survey;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyQueryRepository extends JpaRepository<SurveyQuery, Long> {
}
