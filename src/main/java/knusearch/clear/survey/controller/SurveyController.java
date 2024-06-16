package knusearch.clear.survey.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.service.SearchService;
import knusearch.clear.survey.model.CustomUserDetails;
import knusearch.clear.survey.model.SurveyQuery;
import knusearch.clear.survey.model.SurveyResult;
import knusearch.clear.survey.model.SurveyResultRequest;
import knusearch.clear.survey.service.SurveyQueryService;
import knusearch.clear.survey.service.SurveyResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/survey")
@Tag(name = "Survey", description = "Survey API")
public class SurveyController {

    private final SurveyResultService surveyResultService;
    private final SurveyQueryService surveyQueryService;
    private final SearchService searchService;

    @GetMapping("/login")
    public String loginForm(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "survey/surveyLogin";
    }

    @GetMapping("/page/{queryId}")
    public String showSurveyQuery(@PathVariable("queryId") int queryId,
                                  Authentication authentication,
                                  Model model) {
        SurveyQuery survey = surveyQueryService.findQuery(queryId);
        String query = survey.getQuery();
        List<BasePostRequest> posts = searchService.findTopPostsSortByReverseTime(query);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long participantId = userDetails.getParticipantId();

        List<SurveyResult> surveyResults = surveyResultService.searchByQuery(participantId, queryId);
        surveyResults.stream().forEach(surveyResult -> System.out.println(surveyResult.getParticipantId()+" "+surveyResult.getQueryId()+" "+surveyResult.getSortNumber()));
        List<SurveyResult> surveyResultsWithAI = surveyResultService.searchByQueryWithAI(participantId, queryId);

        model.addAttribute("surveyResults",surveyResults);
        model.addAttribute("surveyResultsWithAI",surveyResultsWithAI);
        model.addAttribute("user",userDetails);
        model.addAttribute("query",query);
        model.addAttribute("queryId",queryId);
        model.addAttribute("posts",posts);

        return "survey/surveyForm";
    }

    @PostMapping("/updateSurveyResult")
    @ResponseBody
    public ResponseEntity<?> updateSurveyResult(@RequestBody SurveyResultRequest surveyResultRequest) {
        //surveyResultService.update() //TODO:

        return ResponseEntity.ok().build();
    }


    //@GetMapping("/updateQueries")
    public String updateQueries() {
        surveyQueryService.loadQueriesFromFile("static/surveyQueries.txt");
        return "hello";
    }

    //@GetMapping("/insertEmptyResults")
    public String insertEmptyResults() {
        surveyResultService.insertEmptyResults();
        return "hello";
    }

}
