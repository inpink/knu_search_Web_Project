package knusearch.clear.jpa.domain.dto;

import static knusearch.clear.constants.StringConstants.UNDETERMINED;

import java.util.NoSuchElementException;

import knusearch.clear.jpa.domain.classification.Classification;
import knusearch.clear.jpa.domain.post.BasePost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class BasePostResponse {
    private final long id;
    private final String title;
    private final String text;
    private final String classification;

    public static BasePostResponse toBasepostResponse(BasePost basePost) {
        return BasePostResponse.builder()
                .id(basePost.getId())
                .title(basePost.getTitle())
                .text(basePost.getText())
                .classification(convertClassification(basePost.getClassification()))
                .build();
    }

    private static String convertClassification(String classification) {
        try {
            return Classification.findDescription(classification);
        } catch (NoSuchElementException noSuchElementException) {
            return UNDETERMINED.getDescription();
        }
    }

}
