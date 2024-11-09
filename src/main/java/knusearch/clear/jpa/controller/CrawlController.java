package knusearch.clear.jpa.controller;

import static knusearch.clear.constants.StringConstants.UNDETERMINED;
import static knusearch.clear.jpa.domain.classification.Classification.ACADEMIC_NOTIFICATION;
import static knusearch.clear.jpa.domain.classification.Classification.EMPLOYMENT_STARTUP;
import static knusearch.clear.jpa.domain.classification.Classification.LEARNING_KNOWHOW;
import static knusearch.clear.jpa.domain.classification.Classification.SCHOLARSHIP;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import knusearch.clear.jpa.domain.classification.Classification;
import knusearch.clear.jpa.domain.dto.BasePostClassifyResponse;
import knusearch.clear.jpa.domain.dto.BasePostResponse;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.post.ClassificationUpdateRequest;
import knusearch.clear.jpa.service.BasePostScrapingFacade;
import knusearch.clear.jpa.service.post.BasePostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

//@Controller
@RequiredArgsConstructor
@Slf4j
public class CrawlController {

    //하나의 controller에서 다른 service들 불러오는건 전혀 문제없음
    private final BasePostService basePostService;
    private final BasePostScrapingFacade basePostScrapingFacade;

    @GetMapping("/testRepo")
    public String testRepo() throws SQLException {
        BasePost basePost = new BasePost();
        basePost.setClassification("1");
        basePostService.savePost(basePost);
        return "hello";
    }

    @GetMapping("/ictCrawlUpdate")
    public String ictCrawlUpdate() {

        basePostScrapingFacade.crawlUpdate();  //이 메소드 성공적으로 완료되는 시점에서 DB에 Commit됨(자세한 내용은 BasePostService참고)
        log.info("KNU ICT POST - 크롤링 업데이트 완료!");

        return "crawlTest";
    }

    @GetMapping("/mainCrawlUpdate")
    public String mainCrawlUpdate() {

        basePostScrapingFacade.crawlUpdate();
        log.info("KNU MAIN POST - 크롤링 업데이트 완료!");
        /*
        2023-09-25기준 25분 소요
        120게시물 담았을때 SHOW TABLE STATUS like 'content_main';로 확인해 봤을때
        Data_length가 98304=98,304 바이트=약 96 KB
         */

        return "crawlTest";
    }

    @GetMapping("/findTextLen/{id}")
    public String findTextLen(@PathVariable long id, Model model) {
        int textLen = basePostService.findPostTextLen(id);

        model.addAttribute("textLen", textLen);
        return "crawlTest";
    }

    @GetMapping("/classify")
    public String classify(Model model, @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BasePost> postPage = basePostService.findAll(pageable);

        List<BasePostResponse> postResponses = postPage.getContent().stream()
                .map(BasePostResponse::toBasepostResponse)
                .toList();

        model.addAttribute("posts", postResponses);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", postPage.getTotalPages());

        List<String> classifications = determineClassOptions();
        model.addAttribute("classOptions", classifications);

        return "classify";
    }


    private static List<String> determineClassOptions() {
        List<String> classifications = new ArrayList<>();

        classifications.add(UNDETERMINED.getDescription());
        classifications.add(ACADEMIC_NOTIFICATION.getDescription());
        classifications.add(SCHOLARSHIP.getDescription());
        classifications.add(LEARNING_KNOWHOW.getDescription());
        classifications.add(EMPLOYMENT_STARTUP.getDescription());

        return classifications;
    }

    private static List<String> determineClassIndexes() {
        List<String> classifications = new ArrayList<>();

        classifications.add(String.valueOf(ACADEMIC_NOTIFICATION.getIndex()));
        classifications.add(String.valueOf(SCHOLARSHIP.getIndex()));
        classifications.add(String.valueOf(LEARNING_KNOWHOW.getIndex()));
        classifications.add(String.valueOf(EMPLOYMENT_STARTUP.getIndex()));

        return classifications;
    }

    @GetMapping("/classify/search")
    public String search() {
        return "classifySearch";
    }

    @GetMapping("/classify/searchResult")
    public String searchResult(Model model,
                               @RequestParam String query,
                               @RequestParam int option) {

        final List<BasePostClassifyResponse> searchedPosts = basePostService.findByQuery(query, option);

        model.addAttribute("searchedPosts", searchedPosts);
        model.addAttribute("query", query);
        model.addAttribute("option", option);

        List<String> classifications = determineClassOptions();
        model.addAttribute("classOptions", classifications);

        List<String> classificationIndexes = determineClassIndexes();
        final List<BasePostClassifyResponse> notInClasses
                = basePostService.findBasePostsNotInClassifications(classificationIndexes);
        model.addAttribute("notInClasses", notInClasses);

        return "classifySearchResult";
    }

    @PostMapping("/classify/update")
    public String update(@RequestParam String query,
                         @RequestParam int option,
                         @RequestParam String except,
                         @RequestParam String classification) throws UnsupportedEncodingException {
        basePostService.updateClassification(query, option, except, transformClassification(classification));

        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        System.out.println("encodedQuery = " + encodedQuery);

        return "redirect:/classify/searchResult?query=" + encodedQuery + "&option=" + option;
    }

    @PostMapping("/api/updateClassification")
    public ResponseEntity<?> updateClassification(@RequestBody ClassificationUpdateRequest request) {
        System.out.println("updateClassification called" + request.getPostId());
        Optional<BasePost> basePost = basePostService.findById(request.getPostId());

        if (basePost.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 ID의 게시물이 존재하지 않습니다.");
        }

        basePostService.updateClassification(
                basePost.get(),
                transformClassification(request.getClassification()));
        return ResponseEntity.ok().build();
    }

    private String transformClassification(final String description) {
        return String.valueOf(Classification.findIndex(description));
    }
}
