package knusearch.clear.jpa.controller;

import java.util.List;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.repository.post.TermRepository;
import knusearch.clear.jpa.service.post.BM25Service;
import knusearch.clear.jpa.service.post.BasePostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection.Base;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import scala.collection.Seq;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BM25Controller {

    private final BasePostService postService;

    private final BasePostRepository postRepository;

    private final BM25Service bm25Service;
    private final BasePostRepository basePostRepository;
    private final TermRepository termRepository;

    @GetMapping("/testest")
    public ResponseEntity<String> test() {
        termRepository.findByNameIn(List.of("test"));
        return ResponseEntity.ok("test");
    }

    @GetMapping("/makeInversedIndex")
    public ResponseEntity<String> createPost() {
        // 시작 시간 측정
        long startTime = System.currentTimeMillis();

        postService.saveAllTermPosts();

        // 종료 시간 측정
        long endTime = System.currentTimeMillis();

        // 소요 시간 계산 (밀리초 단위)
        long duration = endTime - startTime;

        // 시간 출력
        System.out.println("Total time taken: " + duration + " ms");

        return ResponseEntity.ok("success, time taken: " + duration + " ms");
    }

    @GetMapping("/bm25")
    public ResponseEntity<String> calculateBM25(
        @RequestParam String query,
        @RequestParam int postId
    ) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(query);
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);


        List<String> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).stream()
            .filter(token -> (token.getPos().toString().equals("Noun") || token.getPos().toString().equals("ProperNoun"))
                && token.getText().length() > 1
                && !postService.containsSpecialCharacter(token.getText()))
            .map(token -> token.getText())  // 필요한 텍스트만 추출
                .toList();

        System.out.println("words: " + words);

        BasePost post = basePostRepository.findById(Long.valueOf(postId)).get();
        double bm25 =bm25Service.calculateBM25(post,words);
        return ResponseEntity.ok("success"+bm25);
    }

    @GetMapping("/bm25All")
    public ResponseEntity<String> calculateBM25All(
        @RequestParam String query
    ) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(query);
        Seq<KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        List<String> words = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens)
            .stream()
            .filter(token -> (token.getPos().toString().equals("Noun") || token.getPos().toString()
                .equals("ProperNoun"))
                && token.getText().length() > 1
                && !postService.containsSpecialCharacter(token.getText()))
            .map(token -> token.getText())  // 필요한 텍스트만 추출
            .toList();

        System.out.println("words: " + words);

        List<BasePost> posts = basePostRepository.findAll();
        for (BasePost post : posts) {
            double bm25 = bm25Service.calculateBM25(post, words);

            if (bm25 > 0) System.out.println("post: " + post.getId() + ", bm25: " + bm25);
        }
        return ResponseEntity.ok("success");
    }
}

