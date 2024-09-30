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
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.service.post.BM25Service;
import knusearch.clear.jpa.service.post.BasePostService;
import lombok.RequiredArgsConstructor;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import scala.collection.Seq;

@Primary
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

    private final BasePostRepository basePostRepository;
    private final ClassificationService classificationService;
    private final BasePostService basePostService;
    private final BM25Service bm25Service;

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

    public List<BasePostRequest> searchResults(
        List<String> words, String query, int size, Model model) {
        if (query.isBlank()) { // 쿼리 없는 경우 연산 안하고 빈 페이지 반환
            return new ArrayList<>();
        }

        words.add(query);
        for (String word : query.split(" ")) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }

        words.add(query.replace(" ", ""));
        System.out.println("words = " + words);

        List<Map.Entry<BasePostRequest, Integer>> searchResultWithCount = searchAndPostWithoutBoostClassification(words);

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
                + ", sorting without AI Weight: " + weight
                + ", class: " + basePostRequest.classification());
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
                classificationService.findClassification(basePost.classification())
                // 새 classification 값
            ))
            .toList();

        return updatedBasePosts.subList(0, size);
    }


    public List<BasePostRequest> searchResultsWithSortingAlgorithm(
        List<String> words,
        String query,
        String refinedPredictedClass,
        int size,
        Model model,
        int aiWeight
    ) {
        if (query.isBlank()) { // 쿼리 없는 경우 연산 안하고 빈 페이지 반환
            return new ArrayList<>();
        }

        words.add(query);
        for (String word : query.split(" ")) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }

        words.add(query.replace(" ", ""));
        System.out.println("words = " + words);

        List<Map.Entry<BasePostRequest, Integer>> searchResultWithCount = searchAndPostWithBoostClassification(
            words, refinedPredictedClass, aiWeight); //검색어의 분류정보
        model.addAttribute("searchResultWithCount", searchResultWithCount);

        // searchResultWithCount 리스트를 순회하면서 각 항목의 title과 weight만 로그로 출력
        searchResultWithCount.forEach(entry -> {
            BasePostRequest basePostRequest = entry.getKey(); // BasePostRequest 객체
            Integer weight = entry.getValue(); // 해당 객체의 weight

            // BasePostRequest에서 id 가져오기
            Long id = basePostRequest.id();

            // title과 weight만 출력
            System.out.println("Id: " + id
                + ", Sorting+AI Weight: " + weight
                + ", class: " + basePostRequest.classification()
                + ", 일치 개수: " + entry.getValue());
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
                classificationService.findClassification(basePost.classification())
                // 새 classification 값
            ))
            .toList();

        return updatedBasePosts.subList(0, size);
    }

    public List<Map.Entry<BasePostRequest, Integer>> searchAndPostWithBoostClassification(
        List<String> words,
        String classification, int aiWeight) {
        Map<BasePostRequest, Integer> postWithCount = calculateCount(words);
        // 예: 첫 5개의 항목만 출력
        postWithCount.entrySet().stream()
            .limit(5) // 5개의 항목만 선택
            .forEach(entry -> {
                BasePostRequest post = entry.getKey();
                Integer count = entry.getValue();
                System.out.println("시간과 단어 개수 적용 후 Id: " + post.id() + ", Count: " + count);
            });

        Map<BasePostRequest, Integer> postWithCountAndClass = countClassificationWeight(
            postWithCount,
            classification,
            aiWeight);
        // 예: 첫 5개의 항목만 출력
        postWithCount.entrySet().stream()
            .limit(5) // 5개의 항목만 선택
            .forEach(entry -> {
                BasePostRequest post = entry.getKey();
                Integer count = entry.getValue();
                System.out.println("분류 가중치 적용 후 Id: " + post.id() + ", Count: " + count);
            });

        return sortPosts(postWithCountAndClass);
    }

    public Page<BasePostRequest> searchResultsToPage(String categoryRecommendChecked,
        List<String> words,
        String query,
        String refinedPredictedClass,
        int page,
        int size,
        Model model,
        int aiWeight
    ) {
        if (query.isBlank()) { // 쿼리 없는 경우 연산 안하고 빈 페이지 반환
            return listToPage(new ArrayList<>(), page, size);
        }

        words.add(query);
        for (String word : query.split(" ")) {
            if (!word.isBlank()) {
                words.add(word);
            }
        }

        words.add(query.replace(" ", ""));
        System.out.println("words = " + words);

        List<Map.Entry<BasePostRequest, Integer>> searchResultWithCount;

        if (categoryRecommendChecked == null) {
            searchResultWithCount = searchAndPostWithoutBoostClassification(words);
        } else {
            searchResultWithCount = searchAndPostWithBoostClassification(
                words, refinedPredictedClass, aiWeight); //검색어의 분류정보
        }
        // count개수 담은 basepost map 보내기
        model.addAttribute("searchResultWithCount", searchResultWithCount);
        // searchResultWithCount 리스트를 순회하면서 각 항목의 title과 weight만 로그로 출력
        searchResultWithCount.forEach(entry -> {
            BasePostRequest basePostRequest = entry.getKey(); // BasePostRequest 객체

            // BasePostRequest에서 id 가져오기
            Long id = basePostRequest.id(); // record의 경우 직접 필드에 접근할 수 있습니다.

            // title과 weight만 출력
            System.out.println("Id: " + id
                + ", class: " + basePostRequest.classification()
                + ", 일치 개수: " + entry.getValue());
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
                classificationService.findClassification(basePost.classification())
                // 새 classification 값
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


    public List<Map.Entry<BasePostRequest, Integer>> searchAndPostWithoutBoostClassification(
        List<String> words) {
        Map<BasePostRequest, Integer> postWithCount = calculateCount(words);

        return sortPosts(postWithCount);
    }

    private Map<BasePostRequest, Integer> calculateCount(List<String> words) {
        Set<BasePostRequest> allPosts = findAllPostsByTitleAndText(words);

        Map<BasePostRequest, Integer> postWithCount = countQueryOccurrencesInTitles(allPosts,
            words);
        return postWithCount;
    }

    public Map<BasePostRequest, Integer> countQueryOccurrencesInTitles(
        Set<BasePostRequest> allPosts,
        List<String> words) {
        // 게시글 별 점수
        final Map<BasePostRequest, Integer> postCount = new HashMap<>();

        // 단어별 최소 및 최대 등장 횟수 설정
        Map<String, Integer[]> wordMinMaxCounts = new HashMap<>();

        for (String word : words) {
            wordMinMaxCounts.put(word, new Integer[]{0, 0});
        }

        // 게시글별 단어 등장 횟수
        Map<BasePostRequest, Map<String, Integer>> postWordCount = scoreCorrectWordCounts(
            allPosts, words, wordMinMaxCounts);

        // 시간 가중치 계산
        for (Map.Entry<BasePostRequest, Map<String, Integer>> entry : postWordCount.entrySet()) {
            BasePostRequest post = entry.getKey();
            Map<String, Integer> wordCounts = entry.getValue();
            System.out.println(post.id()+" wordCounts = " + wordCounts);
            double score = calculatePostScore(wordCounts, wordMinMaxCounts);
            System.out.println("score = " + score);

            long daysAgo = ChronoUnit.DAYS.between(post.dateTime(), LocalDateTime.now());
            double timeWeight =  Math.exp(-daysAgo / 1000.0); // 나누는 숫자가 커질수록 시간 가중치가 전반적으로 작아짐
            System.out.println("timeWeight = " + timeWeight);
            // 최종 점수에 시간 가중치 반영
            score *= timeWeight;
            System.out.println("score = " + score);
            int intScore = Integer.valueOf((int) (score * 100));
            System.out.println("intScore = " + intScore);

            postCount.put(post,intScore); // 소수점 둘째자리까지 100곱해서 사용
        }

        return postCount;
    }

    private Map<BasePostRequest, Map<String, Integer>> scoreCorrectWordCounts(
        Set<BasePostRequest> allPosts, List<String> words,
        Map<String, Integer[]> wordMinMaxCounts) {
        Map<BasePostRequest, Map<String, Integer>> postWordCount = new HashMap<>();

        for (BasePostRequest post : allPosts) {
            final String title = post.title();
            final String text = post.text();
            Map<String, Integer> wordCount = new HashMap<>(); // 사회 :3 , 복지 : 1 ..

            for (String word : words) { // 사회 복지 학부 졸업
                final int titleCount =
                    (title.length() - title.replace(word, "").length()) / word.length();
                final int textCount =
                    (text.length() - text.replace(word, "").length()) / word.length();
                final int currentCount = (titleCount + textCount);

                Integer[] minMax = wordMinMaxCounts.get(word);
                int min = Math.min(minMax[0], currentCount);
                int max = Math.max(minMax[1], currentCount);

                wordMinMaxCounts.put(word, new Integer[]{min, max});
                wordCount.put(word, currentCount);
            }
            postWordCount.put(post, wordCount);
        }
        return postWordCount;
    }

    private Map<BasePostRequest, Integer> countClassificationWeight(
        Map<BasePostRequest, Integer> postWithCount,
        String classification,
        int weight) {
        Map<BasePostRequest, Integer> withClass = new HashMap<>();

        postWithCount.forEach((post, count) -> {
            if (classification.equals(post.classification())) {
                withClass.put(post, count * weight);
            } else {
                withClass.put(post, count);
            }
        });

        return withClass;
    }


    private Set<BasePostRequest> findAllPostsByTitleAndText(List<String> words) {
        Set<BasePostRequest> allPosts = new HashSet<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

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

    private List<Map.Entry<BasePostRequest, Integer>> sortPosts(
        Map<BasePostRequest, Integer> postWithCount) {
        return postWithCount.entrySet().stream()
            .sorted(Map.Entry.<BasePostRequest, Integer>comparingByValue(Comparator.reverseOrder())
                .thenComparing(entry -> entry.getKey().dateTime(), Comparator.reverseOrder())
                .thenComparing(entry -> entry.getKey().id()))
            .toList();
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

    public List<BasePostRequest> knuPlusAi(String query, String refinedPredictedClass, int size) {
        List<String> words = Arrays.asList(query.split(" "));
        Set<BasePostRequest> allPosts = findAllPostsByTitleAndTextAndSameClass(words,
            refinedPredictedClass);

        List<BasePostRequest> sortedPosts = allPosts.stream()
            .sorted((post1, post2) -> post2.dateTime().compareTo(post1.dateTime()))
            .limit(size)
            .collect(Collectors.toList());

        return sortedPosts;
    }

    private Set<BasePostRequest> findAllPostsByTitleAndTextAndSameClass(List<String> words,
        String predictedClass) {
        Set<BasePostRequest> allPosts = new HashSet<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }

            List<BasePostRequest> posts = basePostRepository.findByTitleOrTextQuery(
                word,
                word);

            for (BasePostRequest post : posts) {
                if (post.classification().equals(predictedClass)) {
                    allPosts.add(post);
                }
            }
        }
        return allPosts;
    }

    public List<BasePostRequest> searchResultsWithBM25(String query) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(query);
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        List<String> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
            .stream()
            .filter(token -> (token.getPos().toString().equals("Noun") || token.getPos().toString()
                .equals("ProperNoun"))
                && token.getText().length() > 1
                && !basePostService.containsSpecialCharacter(token.getText()))
            .map(token -> token.getText())  // 필요한 텍스트만 추출
            .toList();

        System.out.println("words: " + words);

        List<BasePost> posts = bm25Service.getDocuments();

        Map<BasePost, Double> postToScoreMap = new HashMap<>();
        for (BasePost post : posts) {
            double bm25 = bm25Service.calculateBM25(post, words);
            if (bm25 > 0) {
                postToScoreMap.put(post, bm25);
            }
        }

        // BM25 점수에 따라 내림차순으로 정렬
        List<Map.Entry<BasePost, Double>> sortedEntries = postToScoreMap.entrySet()
            .stream()
            .sorted(Map.Entry.<BasePost, Double>comparingByValue().reversed())
            .collect(Collectors.toList());

        // 정렬된 BasePost를 BasePostRequest로 변환
        List<BasePostRequest> result = new ArrayList<>();
        for (Map.Entry<BasePost, Double> entry : sortedEntries) {
            BasePost post = entry.getKey();
            BasePostRequest request = new BasePostRequest(
                post.getId(),
                post.getUrl(),
                post.getTitle(),
                post.getText(),
                post.getImage(),
                post.getDateTime(),
                post.getClassification()
            );
            result.add(request);
        }

        return result;
    }

    public List<BasePostRequest> searchResultsWithBM25PlusAi(String query, String refinedPredictedClass) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(query);
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        List<String> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
            .stream()
            .filter(token -> (token.getPos().toString().equals("Noun") || token.getPos().toString()
                .equals("ProperNoun"))
                && token.getText().length() > 1
                && !basePostService.containsSpecialCharacter(token.getText()))
            .map(token -> token.getText())  // 필요한 텍스트만 추출
            .toList();

        System.out.println("words: " + words);

        List<BasePost> posts = bm25Service.getDocuments();

        Map<BasePost, Double> postToScoreMap = new HashMap<>();
        for (BasePost post : posts) {
            double bm25 = bm25Service.calculateBM25WithAi(post, words, refinedPredictedClass);
            if (bm25 > 0) {
                postToScoreMap.put(post, bm25);
            }
        }

        // BM25 점수에 따라 내림차순으로 정렬
        List<Map.Entry<BasePost, Double>> sortedEntries = postToScoreMap.entrySet()
            .stream()
            .sorted(Map.Entry.<BasePost, Double>comparingByValue().reversed())
            .collect(Collectors.toList());

        // 정렬된 BasePost를 BasePostRequest로 변환
        List<BasePostRequest> result = new ArrayList<>();
        for (Map.Entry<BasePost, Double> entry : sortedEntries) {
            BasePost post = entry.getKey();
            BasePostRequest request = new BasePostRequest(
                post.getId(),
                post.getUrl(),
                post.getTitle(),
                post.getText(),
                post.getImage(),
                post.getDateTime(),
                post.getClassification()
            );
            result.add(request);
        }

        return result;
    }
}
