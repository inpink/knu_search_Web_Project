package knusearch.clear.jpa.service.post;

import java.util.Arrays;
import java.util.List;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.repository.post.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BM25Service {

    private final BasePostRepository postRepository;

    private final TermRepository termRepository;

    private static final double k1 = 1.5;
    private static final double b = 0.75;

    /**
     * 특정 단어의 IDF 값 계산
     * @param term 검색어(단어)
     * @return IDF 값
     */
    public double calculateIDF(String term) {
        long totalDocuments = postRepository.countTotalPosts();
        long documentFrequency = termRepository.countDocumentsWithTerm(term);

        // IDF 계산: log((N / df(term)) + 1)
        return Math.log((double) totalDocuments / (documentFrequency + 1));
    }

    /**
     * BM25 점수 계산
     * @param queryTerms 검색어(쿼리 단어 목록)
     * @param post 게시글
     * @param avgdl 전체 문서의 평균 길이
     * @return BM25 점수
     */
    public double calculateBM25Score(List<String> queryTerms, BasePost post, double avgdl) {
        double score = 0.0;
        String content = post.getTitle()+post.getText();
        int docLength = content.split("\\s+").length; // 문서의 길이 계산

        for (String term : queryTerms) {
            double idf = calculateIDF(term);
            long termFrequency = countTermFrequencyInPost(term, post); // 해당 단어의 빈도

            // BM25 점수 계산
            double tfComponent = (termFrequency * (k1 + 1)) / (termFrequency + k1 * (1 - b + b * (docLength / avgdl)));
            score += idf * tfComponent;
        }

        return score;
    }

    /**
     * 특정 단어의 문서 내 등장 빈도 계산
     * @param term 단어
     * @param post 게시글
     * @return 해당 단어의 빈도
     */
    private long countTermFrequencyInPost(String term, BasePost post) {
        String content = post.getTitle() + post.getText();
        return Arrays.stream(content.split("\\s+"))
            .filter(t -> t.equalsIgnoreCase(term))
            .count();
    }
}
