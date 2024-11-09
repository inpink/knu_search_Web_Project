package knusearch.clear.jpa.domain.site;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum Site {
    MAIN("메인", "https://web.kangnam.ac.kr/menu/", MainBoard.values()),
    ICT("ICT", "https://sae.kangnam.ac.kr/menu/", IctBoard.values()),
    WELFARE("사회복지", "https://knusw.kangnam.ac.kr/menu/", WelfareBoard.values());

    private final String description;
    private final String baseUrl;
    private final List<Board> boards;

    Site(String description, String baseUrl, Board[] boards) {
        this.description = description;
        this.baseUrl = baseUrl;
        this.boards = Arrays.stream(boards).toList();
    }

}
