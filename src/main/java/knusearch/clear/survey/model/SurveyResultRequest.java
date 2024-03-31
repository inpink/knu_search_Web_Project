package knusearch.clear.survey.model;

public record SurveyResultRequest(
        Long participantId,
        Integer queryId,
        Long postId,
        Integer vote) {
}
