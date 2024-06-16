package knusearch.clear;

import knusearch.clear.jpa.domain.BM25;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class ClearApplication {

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(ClearApplication.class, args);

        // BM25
        BM25 bm25 = new BM25();
        /*bm25.addDocument(List.of("hello", "world", "java", "programming"));
        bm25.addDocument(List.of("hello", "java", "code", "project"));
        bm25.addDocument(List.of("java", "example", "hello"));
        bm25.addDocument(List.of("unique", "content", "words"));
        */
        BasePostRepository basePostRepository = context.getBean(BasePostRepository.class);
        for (String post : basePostRepository.findAllTitlesAndTexts()) {
            bm25.updateBm25FromServer(post);
        }
        bm25.computeAvgdl(); // 모든 문서를 추가한 후 평균 문서 길이 계산

        //List<String> query = List.of("java", "project"); // 검색 쿼리
        List<String> query = List.of("중간", "고사"); // 검색 쿼리
        for (int i = 0; i < bm25.documents.size(); i++) {
            double score = bm25.score(query, i); // 각 문서의 점수 계산
            System.out.println("Document " + i + " Score: " + score); // 점수 출력
        }
    }
}
