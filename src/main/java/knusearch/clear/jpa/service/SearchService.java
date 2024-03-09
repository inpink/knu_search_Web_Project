package knusearch.clear.jpa.service;

/*
readOnly 속성은 해당 메서드에서 데이터베이스의 읽기 작업만 수행하고, 쓰기 작업은 하지 않음을 나타냅니다.
이렇게 설정된 메서드는 트랜잭션 커밋 시에 롤백되는 것을 방지하고, 데이터베이스에 대한 읽기 작업을 최적화할 수 있습니다.
 */

import java.util.*;
import java.util.stream.Collectors;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.repository.SearchRepository;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;
    private final BasePostRepository basePostRepository;

    //나중에는 repository(DAO)에서 가져올 듯
    public String findOrder() {
        return "InOrderChecked";
    }

    public String findPeriod() {
        return "allTimeChecked";
    }

    public List<String> findSites() {
        List<String> sites = new ArrayList<>() {{
            add("unifiedSearchChecked");
            add("knuMainSiteChecked");
            add("knuIctSiteChecked");
            add("knuWelfareSiteChecked");
            add("knuSeniorSiteChecked");
            add("knuArtSiteChecked");
            add("knuClasSiteChecked");
            add("knuCtlSiteChecked");
        }};

        return sites;
    }

    public List<Map.Entry<BasePostRequest, Integer>> searchAndPostWithBoostClassification(List<String> words,
                                                                                          String classification) {
        Map<BasePostRequest, Integer> postWithCount = calculateCount(words);

        Map<BasePostRequest, Integer> postWithCountAndClass = countClassificationWeight(
                postWithCount,
                classification);

        return sortPosts(postWithCountAndClass);
    }

    private Map<BasePostRequest, Integer> countClassificationWeight(
            Map<BasePostRequest, Integer> postWithCount,
            String classification) {
        Map<BasePostRequest, Integer> withClass = new HashMap<>();

        /*final int weight = postWithCount.values().stream()
                .max(Integer::compare).get() / 2;*/
        final int weight = 10;

        postWithCount.forEach((post, count) -> {
            if (classification.equals(post.classification())) {
                withClass.put(post, count + weight);
            } else {
                withClass.put(post, count);
            }
        });

        return withClass;
    }

    public List<Map.Entry<BasePostRequest, Integer>> searchAndPosts(List<String> words) {

        Map<BasePostRequest, Integer> postWithCount = calculateCount(words);

        return sortPosts(postWithCount);
    }

    private Map<BasePostRequest, Integer> calculateCount(List<String> words) {
        Set<BasePostRequest> allPosts = new HashSet<>();
        for (String word : words) {
            List<BasePostRequest> posts = basePostRepository.findByTitleOrTextQuery(
                    word,
                    word);

            for (BasePostRequest post : posts) {
                allPosts.add(post);
            }
        }

        Map<BasePostRequest, Integer> postWithCount = countQueryOccurrencesInTitles(allPosts, words);
        return postWithCount;
    }

    private List<Map.Entry<BasePostRequest, Integer>> sortPosts(Map<BasePostRequest, Integer> postWithCount) {
        return postWithCount.entrySet().stream()
                .sorted(Map.Entry.<BasePostRequest, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().dateTime(), Comparator.naturalOrder())
                        .thenComparing(entry -> entry.getKey().id()))
                .toList();
    }

    // RDB에는 일치하는 단어 개수 세어주는 기능 제공하지 않아서 직접 구현해야 함
    public Map<BasePostRequest, Integer> countQueryOccurrencesInTitles(Set<BasePostRequest> allPosts,
                                                                       List<String> words) {
        final Map<BasePostRequest, Integer> postWithCount = new HashMap<>();

        for (BasePostRequest post : allPosts) {
            final String title = post.title();
            final String text = post.text();

            int totalCount = 0;
            for (String word : words) {
                final int titleCount = (title.length() - title.replace(word, "").length()) / word.length();
                final int textCount = (text.length() - text.replace(word, "").length()) / word.length();
                totalCount += (titleCount + textCount);
            }

            postWithCount.put(post, totalCount);
        }

        return postWithCount;
    }
}
