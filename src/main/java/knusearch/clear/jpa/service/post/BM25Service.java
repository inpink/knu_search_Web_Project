package knusearch.clear.jpa.service.post;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.post.PostTerm;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.repository.post.PostTermRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Getter
@Slf4j
public class BM25Service {

    private double k1 = 1.5;  // BM25 조정 파라미터
    private double b = 0.75;  // 문서 길이 보정 파라미터
    public double avgDocLength;  // 평균 문서 길이
    public int totalDocs;  // 전체 문서 수
    public Map<String, Double> idfCache = new ConcurrentHashMap<>();  // 단어별 IDF 캐시
    public Map<Long, Double> docLengthCache = new ConcurrentHashMap<>();  // 문서 길이 캐시

    public Map<String, Integer> docFreqs;  // 단어의 문서 빈도 (IDF 계산용)
    public Map<Long, Map<String, Integer>> docWords = new ConcurrentHashMap<>(); // 문서 별 단어와 단어 빈도

    public List<BasePost> documents;

    private final PostTermRepository postTermRepository;

    public BM25Service(BasePostRepository basePostRepository, PostTermRepository postTermRepository) {
        List<BasePost> documents = basePostRepository.findAll();

        List<PostTerm> postTerms = postTermRepository.findAll();
        for (PostTerm postTerm : postTerms) {
            docWords.computeIfAbsent(postTerm.getPostId(), k -> new HashMap<>())
                .merge(postTerm.getTerm().getName(), 1, Integer::sum);
        }

        postTerms.clear();

        this.totalDocs = documents.size();
        this.avgDocLength = calculateAvgDocLength(documents);  // 평균 문서 길이 캐싱
        this.docFreqs = calculateDocFreqs(documents);  // 단어별 문서 빈도 계산
        this.postTermRepository = postTermRepository;
        this.documents = basePostRepository.findAll();
    }

    // 문서의 평균 길이 계산 (캐싱해둠)
    private double calculateAvgDocLength(List<BasePost> documents) {
        double totalLength = 0.0;
        for (BasePost doc : documents) {
            totalLength += doc.getContent().length();
            docLengthCache.put(doc.getId(), (double) doc.getContent().length());
        }
        return totalLength / documents.size();
    }

    // 각 단어의 문서 빈도 계산 (IDF 계산용)
    private Map<String, Integer> calculateDocFreqs(List<BasePost> documents) {
        Map<String, Integer> frequencies = new HashMap<>();

        for (BasePost doc : documents) {
            Map<String, Integer> wordFreqs = docWords.get(doc.getId());
            if (wordFreqs != null) {
                for (String word : wordFreqs.keySet()) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            }
        }
        return frequencies;
    }

    // 문서의 최신성을 기반으로 가중치 계산 (시간 감쇄 적용)
    private double calculateTimeWeight(BasePost doc) {
        long daysAgo = ChronoUnit.DAYS.between(doc.getDateTime(), LocalDateTime.now());
        return Math.exp(-daysAgo / 1000.0); // 나누는 숫자가 커질수록 시간 가중치가 전반적으로 작아짐
    }

    // BM25 점수 계산
    public double calculateBM25(BasePost doc, List<String> query) {
        double score = 0.0;
        double docLength = getCachedDocLength(doc);  // 문서 길이 캐싱 사용
        double timeWeight = calculateTimeWeight(doc);   // 시간 가중치 계산

        for (String word : query) {
            int termFreq = termFrequency(word, doc);  // 단어 빈도 계산
            if (termFreq == 0) {
                continue;  // 문서에 단어가 없으면 건너뜀
            }

            double idf = getCachedIDF(word);  // IDF 캐싱 사용
            score += idf * ((termFreq * (k1 + 1)) / (termFreq + k1 * (1 - b + b * (docLength
                / avgDocLength))));
        }
        // 시간 가중치를 BM25 점수에 곱해서 반영
        return score * timeWeight;
    }

    // BM25 점수 계산 (AI 적용)
    public double calculateBM25WithAi(BasePost doc, List<String> query,
        String refinedPredictedClass) {
        double score = 0.0;
        double docLength = getCachedDocLength(doc);  // 문서 길이 캐싱 사용
        double timeWeight = calculateTimeWeight(doc);   // 시간 가중치 계산

        for (String word : query) {
            int termFreq = termFrequency(word, doc);  // 단어 빈도 계산
            if (termFreq == 0) {
                continue;  // 문서에 단어가 없으면 건너뜀
            }

            double idf = getCachedIDF(word);  // IDF 캐싱 사용
            score += idf * ((termFreq * (k1 + 1)) / (termFreq + k1 * (1 - b + b * (docLength
                / avgDocLength))));
        }
        // 시간 가중치를 BM25 점수에 곱해서 반영

        score = score * timeWeight;

        if (doc.getClassification().equals(refinedPredictedClass)) {
            score *= 2.0;  // AI 예측이 맞으면 가중치 증가
        }
        return score;
    }

    // 단어 빈도 계산 (TF)
    private int termFrequency(String word, BasePost doc) {
        Map<String, Integer> wordFreqs = docWords.get(doc.getId());
        return wordFreqs != null ? wordFreqs.getOrDefault(word, 0) : 0;
    }

    // 캐싱된 문서 길이 반환
    private double getCachedDocLength(BasePost doc) {
//        System.out.println(doc.getId()+ "docLengthCache: " + docLengthCache.get(doc.getId()));
        return docLengthCache.computeIfAbsent(doc.getId(),
            id -> (double) doc.getContent().length());
    }

    // 캐싱된 IDF 값 반환
    private double getCachedIDF(String term) {
        return idfCache.computeIfAbsent(term, t -> calculateIDF(t));
    }

    // 역문서빈도(IDF) 계산 (캐싱 활용) (루씬 방식)
    private double calculateIDF(String term) {
        int docFreq = docFreqs.getOrDefault(term, 0);
        double idf = Math.log((totalDocs - docFreq + 0.5) / (docFreq + 0.5) + 1);
//        System.out.println("idf: " + idf);
        return idf;
    }


    public void addDocuments(List<BasePost> basePosts) {
        documents.addAll(basePosts);
    }
}
