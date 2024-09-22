package knusearch.clear.service;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.service.SearchService;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
public class searchServiceTest {

    @Mock
    private BasePostRepository basePostRepository;

    @Autowired
    private SearchService searchService;

    /*
    @ParameterizedTest
    @MethodSource("provideTestData")
    void countQueryOccurrencesInTitles(List<String> words, String title1, String text1,
                                       String title2, String text2,
                                       int expectedCount1, int expectedCount2) {
        // Given
        BasePostRequest post1 = new BasePostRequest(1L, "", title1, text1, "",
                LocalDate.now(), "");

        BasePostRequest post2 = new BasePostRequest(1L, "", title2, text2, "",
                LocalDate.now(), "");

        Set<BasePostRequest> posts = new HashSet<>();
        posts.add(post1);
        posts.add(post2);

        // When
        Map<BasePostRequest, Integer> result = searchService.countQueryOccurrencesInTitles(
                posts,
                words);

        // Then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(post1)).isEqualTo(expectedCount1);
        assertThat(result.get(post2)).isEqualTo(expectedCount2);
    }

    private static Stream<Arguments> provideTestData() {
        return Stream.of(
                Arguments.of(
                        List.of("검색 단어"),
                        "ㅎㅎ 검색 단어가 이 문장에 몇 개 있을까요? 검색단어는 띄어쓰기가 달라서 안 됩니다. 검색 단어만 됩니다.",
                        "Nope",
                        "단어 검색 단어 단어단어 ㅋㅋ 단어 단어",
                        "ㅎ단어ㅎ!!!!검색 단어ㅇㅇㅇㅇ검색 단어ㅇㅇ",
                        2, // post1에서 기대되는 검색 단어의 출현 횟수
                        3  // post2에서 기대되는 검색 단어의 출현 횟수
                ),
                Arguments.of(
                        List.of("단어"),
                        "ㅎㅎ 검색 단어가 이 문장에 몇 개 있을까요? 검색단어는 띄어쓰기가 달라서 안 됩니다. 검색 단어만 됩니다.",
                        "단어단어단어단어",
                        "단어 검색 단어 단어단어 ㅋㅋ 단어 단어",
                        "ㅎ단어ㅎ!!!!검색 단어ㅇㅇㅇㅇ검색 단어ㅇㅇ",
                        77, // post1에서 기대되는 검색 단어의 출현 횟수
                        9  // post2에서 기대되는 검색 단어의 출현 횟수
                )

        );
    }
*/
    @Test
    public void searchAndPostsReturnsSortedMap() {
        // Given
        String searchQuery = "test";
        BasePostRequest post1 = new BasePostRequest(1L, "", "test", "", "",
                LocalDate.now().minusDays(1), "");

        BasePostRequest post2 = new BasePostRequest(1L, "", "test test", "", "",
                LocalDate.now().minusDays(1), "");

        when(basePostRepository.findByTitleOrTextQuery(searchQuery, searchQuery))
                .thenReturn(Arrays.asList(post1, post2));

        // When
        List<Map.Entry<BasePostRequest, Integer>> result = searchService.searchAndPostWithoutBoostClassification(
                List.of(searchQuery));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 가정: countQueryOccurrencesInTitles 로직에 따라 결과가 있음

        // 검증: 결과가 정렬되어 있어야 함, 가장 최신 post가 먼저 나와야 함
        BasePostRequest firstEntry = result.stream()
                .map(Map.Entry::getKey)
                .toList().get(0);
        assertEquals(post2, firstEntry); // 가장 최신 post가 맨 처음에 오는지 확인
    }

}
