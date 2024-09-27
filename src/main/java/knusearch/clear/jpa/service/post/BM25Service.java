package knusearch.clear.jpa.service.post;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.post.PostTerm;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.repository.post.PostTermRepository;
import org.springframework.stereotype.Service;

@Service
public class BM25Service {

    private double k1 = 1.5;  // BM25 조정 파라미터
    private double b = 0.75;  // 문서 길이 보정 파라미터
    private double avgDocLength;  // 평균 문서 길이
    private int totalDocs;  // 전체 문서 수
    private Map<String, Double> idfCache = new ConcurrentHashMap<>();  // 단어별 IDF 캐시
    private Map<Long, Double> docLengthCache = new ConcurrentHashMap<>();  // 문서 길이 캐시

    private Map<String, Integer> docFreqs;  // 단어의 문서 빈도 (IDF 계산용)
    private Map<Long, Map<String, Integer>> docWords = new ConcurrentHashMap<>(); // 문서 별 단어와 단어 빈도

    private BasePostRepository basePostRepository;
    private final PostTermRepository postTermRepository;

    public BM25Service(BasePostRepository basePostRepository, PostTermRepository postTermRepository) {
        List<BasePost> documents = basePostRepository.findAll();

        List<PostTerm> postTerms = postTermRepository.findAll();
        for (PostTerm postTerm : postTerms) {
            docWords.computeIfAbsent(postTerm.getPostId(), k -> new HashMap<>())
                .merge(postTerm.getTerm().getName(), 1, Integer::sum);
        }

        postTerms.clear();
        postTerms = null;

        this.totalDocs = documents.size();
        this.avgDocLength = calculateAvgDocLength(documents);  // 평균 문서 길이 캐싱
        this.docFreqs = calculateDocFreqs(documents);  // 단어별 문서 빈도 계산
        this.postTermRepository = postTermRepository;

//        System.out.println("totalDocs: " + totalDocs);
//        System.out.println("avgDocLength: " + avgDocLength);

//        System.out.println("docWords: " + docWords.get(1L));
//        System.out.println("docWords: " + docWords.get(2L));
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

    // BM25 점수 계산
    public double calculateBM25(BasePost doc, List<String> query) {
        double score = 0.0;
        double docLength = getCachedDocLength(doc);  // 문서 길이 캐싱 사용

        for (String word : query) {
            int termFreq = termFrequency(word, doc);  // 단어 빈도 계산
            if (termFreq == 0) continue;  // 문서에 단어가 없으면 건너뜀

            double idf = getCachedIDF(word);  // IDF 캐싱 사용
            score += idf * ((termFreq * (k1 + 1)) / (termFreq + k1 * (1 - b + b * (docLength / avgDocLength))));
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
        return docLengthCache.computeIfAbsent(doc.getId(), id -> (double) doc.getContent().length());
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
}
