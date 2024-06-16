package knusearch.clear.jpa.controller;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.service.SearchService;
import knusearch.clear.survey.service.SurveyQueryService;
import knusearch.clear.survey.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ResearchController {

    private final SurveyResultService surveyResultService;
    private final SurveyQueryService surveyQueryService;
    private final SearchService searchService; //테스트용이라 바꿔야함

    @GetMapping("/research")
    public String researchPage(Model model) {
        List<BasePostRequest> beforeKNU = new ArrayList<>();
        List<BasePostRequest> bm25 = new ArrayList<>();
        List<BasePostRequest> bm25AndAI = new ArrayList<>();
        List<BasePostRequest> AIAndBm25 = new ArrayList<>();

        model.addAttribute("beforeKNU", beforeKNU);
        model.addAttribute("bm25", bm25);
        model.addAttribute("bm25AndAI", bm25AndAI);
        model.addAttribute("AIAndBm25", AIAndBm25);

        return "research/researchPage";
    }

    @GetMapping("/research/result")
    public String researchResult(@RequestParam("query") String query,
                                     Model model) {
        List<BasePostRequest> beforeKNU = searchService.findTopPostsSortByReverseTime(query);
        List<BasePostRequest> bm25 = searchService.findTopPostsSortByBM25(query);
        List<BasePostRequest> bm25AndAI = new ArrayList<>();
        List<BasePostRequest> AIAndBm25 = new ArrayList<>();

        model.addAttribute("beforeKNU", beforeKNU);
        model.addAttribute("bm25", bm25);
        model.addAttribute("bm25AndAI", bm25AndAI);
        model.addAttribute("AIAndBm25", AIAndBm25);

        return "research/researchPage";
    }
}