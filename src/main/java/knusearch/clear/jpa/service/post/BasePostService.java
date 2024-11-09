package knusearch.clear.jpa.service.post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import knusearch.clear.jpa.domain.dto.BasePostClassifyResponse;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.post.PostTerm;
import knusearch.clear.jpa.domain.post.Term;
import knusearch.clear.jpa.domain.site.Board;
import knusearch.clear.jpa.domain.site.Site;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.repository.post.PostTermJdbcRepository;
import knusearch.clear.jpa.repository.post.TermJdbcRepository;
import knusearch.clear.jpa.repository.post.TermRepository;
import knusearch.clear.jpa.service.ScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.Seq;

import static knusearch.clear.jpa.domain.classification.SearchOption.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasePostService {

    // postService들 -> cralwSerivce 접근OK,
    // 반대로  cralwSerivce -> postService는 절대 금지(순환참조). 순환참조는 하면 안 됨
    private final BasePostRepository basePostRepository;
    private final TermRepository termRepository;
    private final BM25Service bm25Service;

    // 스레드 안전한 ConcurrentHashMap을 사용하여 Term 캐싱
    private final ConcurrentMap<String, Term> termCache = new ConcurrentHashMap<>();
    private final PostTermJdbcRepository postTermJdbcRepository;
    private final TermJdbcRepository termJdbcRepository;

    //Transactional을 먹여줘서, CrawlController의 하나의 method에서 요구한 모든 작업이 끝난 뒤에 DB에 Commit된다!
    //CrawlController의 하나의 메소드에서는 postIctService.crawlUpdate(); 이런식으로 호출되었다.
    //이 메소드가 완전히 끝나야만 DB에 Commit된다. 그 전에 작업 중에는 repo.save가 실행돼도 실제로 DB에 반영되지 않는다는 것이다!
    // 배치 크기 설정
    private static final int BATCH_SIZE = 500;

    @Transactional
    public void saveAllTermPosts() {
        List<BasePost> basePosts = basePostRepository.findAll();
        tokenizeAndSaveBasePostTerms(basePosts);
    }

    public void tokenizeAndSaveBasePostTerms(List<BasePost> basePosts) {
        List<PostTerm> postTermsBatch = new ArrayList<>();
        for (BasePost post : basePosts) {
            List<PostTerm> postTerms = saveTermPost(post);
            postTermsBatch.addAll(postTerms);

            if (postTermsBatch.size() >= BATCH_SIZE) {
                // postTermsBatch의 복사본을 사용하여 비동기 작업을 동시에 실행
                List<PostTerm> batchCopy = new ArrayList<>(postTermsBatch);
                savePostTermsAsync(batchCopy);
                updateBM25Async(batchCopy);

                postTermsBatch.clear();
            }
        }

        if (!postTermsBatch.isEmpty()) {
            List<PostTerm> batchCopy = new ArrayList<>(postTermsBatch);
            savePostTermsAsync(batchCopy);
            updateBM25Async(batchCopy);
        }
    }

    // 1번 작업: PostTerm을 저장 (비동기 및 재시도)
    @Async
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public CompletableFuture<Void> savePostTermsAsync(List<PostTerm> postTermsBatch) {
        return CompletableFuture.runAsync(() -> {
            try {
                postTermJdbcRepository.saveAll(postTermsBatch);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 2번 작업: BM25 업데이트 (비동기 및 재시도)
    @Async
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public CompletableFuture<Void> updateBM25Async(List<PostTerm> postTermsBatch) {
        return CompletableFuture.runAsync(() -> {
            try {
                // TODO: 캐시 업데이트
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenRun(() -> {
            // TODO: 후속 작업을 이곳에 추가
            log.info("BM25 update completed successfully for batch.");
        });
    }

    /**
     * 게시글을 저장하고, 해당 게시글의 단어를 추출하여 저장
     *
     * @param post 게시글 객체
     * @return 저장할 PostTerm 리스트
     */
    private List<PostTerm> saveTermPost(BasePost post) {
        // 게시글의 내용을 분석하여 단어를 추출
        Set<Term> terms = extractTermsFromContent(post.getTitle() + post.getText());

        List<String> termTexts = terms.stream()
            .map(Term::getName)
            .collect(Collectors.toList());

        // 캐시에서 먼저 단어 확인
        Map<String, Term> cachedTerms = new HashMap<>();
        List<String> missingTerms = new ArrayList<>();

        for (String termText : termTexts) {
            Term cachedTerm = termCache.get(termText);
            if (cachedTerm != null) {
                cachedTerms.put(termText, cachedTerm);
            } else {
                missingTerms.add(termText); // 캐시에 없는 단어 수집
            }
        }

        // 캐시에 없는 단어들을 한 번에 DB에서 조회
        if (!missingTerms.isEmpty()) {
            List<Term> foundTerms = termRepository.findByNameIn(missingTerms);
            Map<String, Term> foundTermsMap = foundTerms.stream()
                .collect(Collectors.toMap(Term::getName, term -> term));

            // DB에서 찾은 단어는 캐시에 추가
            foundTermsMap.forEach((key, value) -> {
                cachedTerms.put(key, value);
                termCache.put(key, value);  // 캐시에 저장
            });

            // DB에도 없던 단어는 새로 생성하고 캐시에 추가
            missingTerms.removeAll(foundTermsMap.keySet());  // 이미 DB에서 찾은 단어는 제외
            List<Term> newTerms = new ArrayList<>();
            for (String termText : missingTerms) {
                Term newTerm = new Term();
                newTerm.setName(termText);
                newTerms.add(newTerm);
                cachedTerms.put(termText, newTerm);
                termCache.put(termText, newTerm);  // 캐시에 저장
            }

            termJdbcRepository.saveAll(newTerms);
        }

        // Post와 Term 연관
        List<PostTerm> postTerms = new ArrayList<>();
        for (Term term : cachedTerms.values()) {
            PostTerm postTerm = new PostTerm();
            postTerm.setBasePost(post);
            postTerm.setTerm(term);

            postTerms.add(postTerm);
        }

        return postTerms; // 생성한 PostTerm 리스트 반환
    }


    // 특수문자에 대한 필터링 조건
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+{}[]|\\:;<>,.?/~";

    /**
     * 게시글의 내용에서 형태소 분석을 통해 단어를 추출
     *
     * @param content 게시글 내용
     * @return 단어 집합
     */
    public Set<Term> extractTermsFromContent(String content) {
        // 텍스트를 정규화
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(content);

        // 형태소 분석을 수행하여 토큰화
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        // 단어 집합을 담을 Set 생성
        Set<Term> terms = new HashSet<>();

        // 각 토큰을 Term으로 변환하여 Set에 추가
        OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).forEach(token -> {
            // 조건: 명사이면서, 길이가 1보다 크고, 특수문자가 포함되지 않은 경우
            if ((token.getPos().toString().equals("Noun") || token.getPos().toString()
                .equals("ProperNoun"))
                && token.getText().length() > 1
                && !containsSpecialCharacter(token.getText())) {

                Term term = new Term();
                term.setName(token.getText());  // 토큰의 텍스트를 Term에 저장
                terms.add(term);
            }
        });

        return terms;
    }

    /**
     * 주어진 문자열에 특수문자가 포함되어 있는지 확인하는 메서드
     *
     * @param text 확인할 텍스트
     * @return 특수문자가 포함되어 있으면 true, 아니면 false
     */
    public boolean containsSpecialCharacter(String text) {
        for (char c : text.toCharArray()) {
            if (SPECIAL_CHARACTERS.indexOf(c) >= 0) {
                return true;  // 특수문자가 있으면 true 반환
            }
        }
        return false;  // 특수문자가 없으면 false 반환
    }



    @Transactional
    public void savePost(BasePost basePost) {
        basePostRepository.save(basePost);
    }

    @Transactional
    public int findPostTextLen(long id) {
        int len = 0;

        //JPA의 em.find 메서드를 사용하여 엔티티를 검색할 때, 해당 ID에 해당하는 엔티티가 데이터베이스에 없는 경우 null을 반환
        Optional<BasePost> basePost = basePostRepository.findById(id);
        if (basePost != null) { //따라서 null 여부를 확인하여 NullPointerException을 방지할 수 있다
            String text = basePost.get().getText();
            len = text.length();
        }

        return len;
    }

    @Transactional
    public List<BasePost> findAll() {
        return basePostRepository.findAll();
    }

    @Transactional
    public Page<BasePost> findAll(Pageable pageable) {
        return basePostRepository.findAll(pageable);
    }

    @Transactional
    public Optional<BasePost> findById(Long postId) {
        return basePostRepository.findById(postId);
    }

    @Transactional
    public List<BasePost> findAllByQuery(String query, int option) {
        List<BasePost> basePosts = new ArrayList<>();

        if (TITLE.getIndex() == option) {
            basePosts = basePostRepository.findAllByTitleContaining(query);
        }
        if (TEXT.getIndex() == option) {
            basePosts = basePostRepository.findAllByTextContaining(query);
        }
        /*if (TITLE_AND_TEXT.getIndex() == option) {
            basePosts = basePostRepository.findByTitleOrTextQuery(query, query);
        }*/

        return basePosts;
    }

    // DTO 변환을 Service에서 하되, 기존 Entity로 받아오는 코드와 변환 코드를 따로 둬서 재사용성을 높임
    @Transactional
    public List<BasePostClassifyResponse> findByQuery(String query, int option) {
        List<BasePost> basePosts = findAllByQuery(query, option);

        if (basePosts.isEmpty()) {
            return generateEmptyResponse();
        }

        return transformToClassifyResponse(basePosts);
    }

    private static List<BasePostClassifyResponse> transformToClassifyResponse(
        List<BasePost> basePosts) {
        return basePosts.stream()
            .map(basePost -> new BasePostClassifyResponse(
                basePost.getId(),
                basePost.getClassification(),
                basePost.getTitle()))
            .toList();
    }

    private static ArrayList<BasePostClassifyResponse> generateEmptyResponse() {
        return new ArrayList<>();
    }

    @Transactional
    public void updateClassification(BasePost basePost, String classification) {
        basePost.setClassification(classification);
    }

    @Transactional
    public void updateClassification(String query, int option, String except,
        String classification) {
        List<BasePost> posts = new ArrayList<>();
        if (except.isEmpty()) {
            posts = findAllByQuery(query, option);
        } else {
            if (TITLE.getIndex() == option) {
                posts = basePostRepository.findByTitleQueryExcept(query, except);
            }
            if (TEXT.getIndex() == option) {
                posts = basePostRepository.findByTextQueryExcept(query, except);
            }
            if (TITLE_AND_TEXT.getIndex() == option) {
                posts = basePostRepository.findByTitleOrTextQueryExcept(query, query, except);
            }
        }

        if (posts.isEmpty()) {
            return;
        }

        posts.stream()
            .forEach(post -> updateClassification(post, classification));
    }

    @Transactional
    public List<BasePostClassifyResponse> findBasePostsNotInClassifications(
        List<String> classifications) {
        List<BasePost> posts = basePostRepository.findBasePostsNotInClassifications(
            classifications,
            PageRequest.of(0, 5));

        return transformToClassifyResponse(posts);
    }

    @Retryable(
        value = {BM25UpdateException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    @Transactional
    public void updateIndex(List<BasePost> basePosts) {
        try {
            bm25Service.addDocuments(basePosts);
            tokenizeAndSaveBasePostTerms(basePosts);
        } catch (Exception e) {
            throw new BM25UpdateException("BM25 update failed", e);
        }
    }

    public String getBaseUrl() {
        return Site.MAIN.getBaseUrl();
    }

    public List<Board> getBoards() {
        return Site.MAIN.getBoards();
    }
}
