package knusearch.clear.survey.repository;

import knusearch.clear.survey.model.SurveyResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SurveyResultRepository extends JpaRepository<SurveyResult, Long> {
    List<SurveyResult> findAllByParticipantIdAndQueryId(Long participantId, int queryId);

}
