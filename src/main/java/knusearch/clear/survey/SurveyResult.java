package knusearch.clear.survey;

import jakarta.persistence.*;

@Entity
public class SurveyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long queryId;

    private int sortId;

    private boolean sortScore;
}
