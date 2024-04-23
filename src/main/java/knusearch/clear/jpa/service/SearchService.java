package knusearch.clear.jpa.service;

/*
readOnly 속성은 해당 메서드에서 데이터베이스의 읽기 작업만 수행하고, 쓰기 작업은 하지 않음을 나타냅니다.
이렇게 설정된 메서드는 트랜잭션 커밋 시에 롤백되는 것을 방지하고, 데이터베이스에 대한 읽기 작업을 최적화할 수 있습니다.
 */

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

@Primary
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

    private final BasePostRepository basePostRepository;
    private final ClassificationService classificationService;

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

    public Page<BasePostRequest> searchResults(String categoryRecommendChecked,
                                               List<String> words,
                                               String query,
                                               String refinedPredictedClass,
                                               int page,
                                               int size,
                                               Model model
    ) {
        if (query.isBlank()) { // 쿼리 없는 경우 연산 안하고 빈 페이지 반환
            return listToPage(new ArrayList<>(), page, size);
        }

        words.add(query);
        for (String word : query.split(" ")){
            if (!word.isBlank()) {
                words.add(word);
            }
        }

        words.add(query.replace(" ",""));
        System.out.println("words = " + words);

        List<Map.Entry<BasePostRequest, Integer>> searchResultWithCount;

        if (categoryRecommendChecked==null) {
            System.out.println("분류 사용 X");
            searchResultWithCount = searchAndPosts(words);
        } else {
            System.out.println("분류 사용 O");
            searchResultWithCount = searchAndPostWithBoostClassification(
                    words, refinedPredictedClass); //검색어의 분류정보
        }
        // count개수 담은 basepost map 보내기
        model.addAttribute("searchResultWithCount", searchResultWithCount);
        // searchResultWithCount 리스트를 순회하면서 각 항목의 title과 weight만 로그로 출력
        searchResultWithCount.forEach(entry -> {
            BasePostRequest basePostRequest = entry.getKey(); // BasePostRequest 객체
            Integer weight = entry.getValue(); // 해당 객체의 weight

            // BasePostRequest에서 id 가져오기
            Long id = basePostRequest.id(); // record의 경우 직접 필드에 접근할 수 있습니다.

            // title과 weight만 출력
            System.out.println("Id: " + id
                    + ", Weight: " + weight
                    + ", class: " + basePostRequest.classification()
                    + ", 일치 개수: "+ entry.getValue());
        });

        // basepost만 따로 리스트로 추출하여 페이지로 보내기
        List<BasePostRequest> basePosts = searchResultWithCount.stream()
                .map(Map.Entry::getKey)
                .toList();

        // front에서 쉽게 읽을 수 있도록 class 한글로 변환 (record는 불변)
        List<BasePostRequest> updatedBasePosts = basePosts.stream()
                .map(basePost -> new BasePostRequest(
                        basePost.id(),
                        basePost.url(),
                        basePost.title(),
                        basePost.text(),
                        basePost.image(),
                        basePost.dateTime(),
                        classificationService.findClassification(basePost.classification()) // 새 classification 값
                ))
                .toList();

        return listToPage(updatedBasePosts, page, size);
    }

    private Page<BasePostRequest> listToPage(List<BasePostRequest> list, int page, int size) {
        // 시작 인덱스 계산
        int start = Math.min(page * size, list.size());
        // 종료 인덱스 계산
        int end = Math.min((start + size), list.size());
        // 서브리스트 생성
        List<BasePostRequest> subList = list.subList(start, end);
        // PageRequest 객체 생성, 페이지 번호는 0부터 시작하므로 1을 빼줘야 한다는 점에 유의
        PageRequest pageRequest = PageRequest.of(page, size);
        // PageImpl 객체 생성 및 반환
        return new PageImpl<>(subList, pageRequest, list.size());
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
        final int weight = 12;

        postWithCount.forEach((post, count) -> {
            if (classification.equals(post.classification())) {
                withClass.put(post, count * weight / 10);
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
        Set<BasePostRequest> allPosts = findAllPostsByTitleAndText(words);

        Map<BasePostRequest, Integer> postWithCount = countQueryOccurrencesInTitles(allPosts, words);
        return postWithCount;
    }

    private Set<BasePostRequest> findAllPostsByTitleAndText(List<String> words) {
        Set<BasePostRequest> allPosts = new HashSet<>();
        for (String word : words) {
            if (word.isBlank()) continue;

            List<BasePostRequest> posts = basePostRepository.findByTitleOrTextQuery(
                    word,
                    word);

            for (BasePostRequest post : posts) {
                allPosts.add(post);
            }
        }
        return allPosts;
    }

    public List<BasePostRequest> findTopPostsSortByReverseTime(String query) {
        List<String> words = Arrays.asList(query.split(" "));
        Set<BasePostRequest> allPosts = findAllPostsByTitleAndText(words);

        List<BasePostRequest> sortedPosts = allPosts.stream()
                .sorted((post1, post2) -> post2.dateTime().compareTo(post1.dateTime()))
                .limit(5)
                .collect(Collectors.toList());

        return sortedPosts;
    }

    public List<BasePostRequest> findTopPostsSortByBM25(String query) {
        //TODO : BM25 구현 연결
        return new ArrayList<>();
    }

    private List<Map.Entry<BasePostRequest, Integer>> sortPosts(Map<BasePostRequest, Integer> postWithCount) {
        return postWithCount.entrySet().stream()
                .sorted(Map.Entry.<BasePostRequest, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().dateTime(), Comparator.reverseOrder())
                        .thenComparing(entry -> entry.getKey().id()))
                .toList();
    }

    public Map<BasePostRequest, Integer> countQueryOccurrencesInTitles(Set<BasePostRequest> allPosts,
                                                                       List<String> words) {
        // 게시글 별 점수
        final Map<BasePostRequest, Integer> postCount = new HashMap<>();

        // 단어별 최소 및 최대 등장 횟수 설정
        Map<String, Integer[]> wordMinMaxCounts = new HashMap<>();

        for (String word : words) {
            wordMinMaxCounts.put(word, new Integer[]{0, 0});
        }

        // 게시글별 단어 등장 횟수
        Map<BasePostRequest, Map<String, Integer>> postWordCount = new HashMap<>();

        for (BasePostRequest post : allPosts) {
            final String title = post.title();
            final String text = post.text();
            Map<String, Integer> wordCount = new HashMap<>(); // 사회 :3 , 복지 : 1 ..

            for (String word : words) { // 사회 복지 학부 졸업
                final int titleCount = (title.length() - title.replace(word, "").length()) / word.length();
                final int textCount = (text.length() - text.replace(word, "").length()) / word.length();
                final int currentCount = (titleCount + textCount);

                Integer[] minMax = wordMinMaxCounts.get(word);
                int min = Math.min(minMax[0], currentCount);
                int max = Math.max(minMax[1], currentCount);

                wordMinMaxCounts.put(word, new Integer[]{min, max});
                wordCount.put(word, currentCount);
            }
            postWordCount.put(post, wordCount);
        }

        for (Map.Entry<BasePostRequest, Map<String, Integer>> entry : postWordCount.entrySet()) {
            BasePostRequest post = entry.getKey();
            Map<String, Integer> wordCounts = entry.getValue();
            double score = calculatePostScore(wordCounts, wordMinMaxCounts);

            // 시간 가중치 계산
            long daysAgo = ChronoUnit.DAYS.between(post.dateTime(), LocalDateTime.now());
            double timeWeight = 1.0 / (Math.log(1.0 + daysAgo) + 1); // 로그 함수 사용하여 가중치 조절

            // 최종 점수에 시간 가중치 반영
            score *= timeWeight;

            postCount.put(post, Integer.valueOf((int) (score * 100))); // 소수점 둘째자리까지 100곱해서 사용
        }

        System.out.println("wordMinMaxCounts = ");
        for (Map.Entry<String, Integer[]> entry : wordMinMaxCounts.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue()[0] + " " + entry.getValue()[1]);
        }

/*        System.out.println("postCount = ");
        for (Map.Entry<BasePostRequest, Integer> entry : postCount.entrySet()) {
            System.out.println(entry.getKey().title() + " " + entry.getValue());
        }*/
        return postCount;
    }

    // 정규화 점수
    private static double calculatePostScore(Map<String, Integer> postCounts,
                                             Map<String, Integer[]> wordMinMaxCounts) {
        double score = 0.0;

        for (Map.Entry<String, Integer> entry : postCounts.entrySet()) {
            String word = entry.getKey();
            Integer count = entry.getValue();
            Integer[] minMax = wordMinMaxCounts.get(word);
            if (minMax != null) {
                double normalized = normalize(count, minMax[0], minMax[1]);
                score += normalized;
            }
        }

        return score;
    }

    // 선형 정규화 공식
    private static double normalize(int value, int min, int max) {
        if (max == 0) {
            return 0;
        }
        return (double) (value - min) / (max - min);
    }
}
