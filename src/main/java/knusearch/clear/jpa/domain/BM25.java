package knusearch.clear.jpa.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BM25 {
    private List<Map<String, Integer>> documents; // 문서와 그에 해당하는 단어의 빈도를 저장하는 리스트
    private Map<String, Integer> docFreq; // 각 단어가 몇 개의 문서에 등장하는지 저장하는 맵
    private double avgdl; // 모든 문서의 평균 길이
    private int totalDocs; // 총 문서 수
    private static final double k1 = 1.2; // BM25 공식의 k1 파라미터
    private static final double b = 0.75; // BM25 공식의 b 파라미터

    public BM25() {
        documents = new ArrayList<>();
        docFreq = new HashMap<>();
        totalDocs = 0;
        avgdl = 0;
    }

    public void addDocument(List<String> words) {
        Map<String, Integer> freqs = new HashMap<>();
        for (String word : words) {
            freqs.put(word, freqs.getOrDefault(word, 0) + 1); // 단어 빈도 수 갱신
            docFreq.put(word, docFreq.getOrDefault(word, 0) + 1); // 문서 빈도 수 갱신
        }
        documents.add(freqs); // 문서 리스트에 현재 문서의 단어 빈도 맵 추가
        totalDocs++; // 문서 수 증가
        avgdl += words.size(); // 전체 단어 수 업데이트
    }

    public void computeAvgdl() {
        if (totalDocs > 0) {
            avgdl /= totalDocs; // 전체 문서의 평균 길이 계산
        }
    }

    private double idf(String term) {
        // 주어진 용어의 IDF 값을 계산하는 함수
        return Math.log((totalDocs + 1) / (double)(docFreq.getOrDefault(term, 0) + 1)) + 1;
    }

    public double score(List<String> query, int docIndex) {
        Map<String, Integer> doc = documents.get(docIndex); // 점수를 계산할 문서 선택
        double score = 0.0;

        for (String word : query) { // 쿼리의 각 단어에 대해
            if (doc.containsKey(word)) { // 문서에 단어가 포함되어 있으면
                double idf = idf(word); // 단어의 IDF 값 계산
                int tf = doc.get(word); // 문서에서 단어의 빈도 수(tf)
                double numerator = tf * (k1 + 1); // 점수 계산의 분자 부분
                double denominator = tf + k1 * (1 - b + b * doc.size() / avgdl); // 점수 계산의 분모 부분
                score += idf * (numerator / denominator); // 총 점수에 추가
            }
        }
        return score; // 계산된 점수 반환
    }

    public static void main(String[] args) {
        BM25 bm25 = new BM25();
        bm25.addDocument(List.of("hello", "world", "java", "programming"));
        bm25.addDocument(List.of("hello", "java", "code", "project"));
        bm25.addDocument(List.of("java", "example", "hello"));
        bm25.addDocument(List.of("unique", "content", "words"));
        bm25.computeAvgdl(); // 모든 문서를 추가한 후 평균 문서 길이 계산

        List<String> query = List.of("java", "project"); // 검색 쿼리
        for (int i = 0; i < bm25.documents.size(); i++) {
            double score = bm25.score(query, i); // 각 문서의 점수 계산
            System.out.println("Document " + i + " Score: " + score); // 점수 출력
        }
    }
}
