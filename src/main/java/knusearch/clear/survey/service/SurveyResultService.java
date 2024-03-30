package knusearch.clear.survey.service;

import knusearch.clear.survey.model.SurveyResult;
import knusearch.clear.survey.repository.SurveyResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SurveyResultService {

    private final SurveyResultRepository surveyResultRepository;

    @Transactional
    public void insertEmptyResults() {

        for (int queryId = 1; queryId <= 100; queryId++) {
            for (int participantId = 1; participantId <= 5; participantId++) {
                for (int sortNumber = 1; sortNumber <= 5; sortNumber++) {
                    SurveyResult surveyResult = new SurveyResult();

                    surveyResult.setQueryId(Integer.toUnsignedLong(queryId));
                    surveyResult.setParticipantId(Integer.toUnsignedLong(participantId));
                    surveyResult.setSortNumber(sortNumber);
                    surveyResult.setWithAiScore(-1); // 초기값 -1, 유사도낮음 0, 유사도 높음 1
                    surveyResult.setWithoutAiScore(-1);

                    surveyResultRepository.save(surveyResult);
                }
            }
        }
    }
}
