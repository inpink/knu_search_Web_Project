package knusearch.clear.jpa.controller;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.service.ClassificationService;
import knusearch.clear.jpa.service.SearchService;
import knusearch.clear.survey.service.SurveyQueryService;
import knusearch.clear.survey.service.SurveyResultService;
import knusearch.clear.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ResearchController {

    private final ClassificationService classificationService;
    private final SearchService searchService; //테스트용이라 바꿔야함

    @GetMapping("/research")
    public String researchPage(Model model) {
        List<BasePostRequest> beforeKNU = new ArrayList<>();
        List<BasePostRequest> resnetTransformer = new ArrayList<>();
        model.addAttribute("beforeKNU", beforeKNU);

        return "research/researchPage";
    }

    @GetMapping("/research/result")
    public String researchResult(@RequestParam("query") String query, Model model) {
        if (!query.matches("[가-힣\\s]+")) {
            return "research/researchErrorPage";
        }

        List<BasePostRequest> beforeKNU = searchService.findTopPostsSortByReverseTime(query);

        Map<String, Object> predictedAndTokens = classificationService.predictClassification(query);
        String predictedClass = (String) predictedAndTokens.get("predictedClass");
        List<String> words = (List<String>) predictedAndTokens.get("words");
        String refinedPredictedClass = StringUtil.deleteLineSeparator(predictedClass);

        model.addAttribute("predictedClass", predictedClass);

        List<BasePostRequest> knuPlusAi = searchService.knuPlusAi(
            query,
            refinedPredictedClass,
            5);

        List<BasePostRequest> sortingAlgorithm = searchService.searchResults(
                words,
                query,
                5,
                model);

        List<BasePostRequest>  sortingResnetTransformer = searchService.searchResultsWithSortingAlgorithm(
                words,
                query,
                refinedPredictedClass,
                5,
                model
        );

        // 가중치 3인 버전

        // bm25만

        // bm25+ AI classi



        model.addAttribute("beforeKNU", beforeKNU);
        model.addAttribute("knuPlusAi", knuPlusAi);
        model.addAttribute("sortingAlgorithm", sortingAlgorithm);
        model.addAttribute("resnetTransformer", sortingResnetTransformer);

        return "research/researchPage";
    }
}
