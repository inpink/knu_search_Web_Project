package knusearch.clear.survey;

import jakarta.servlet.http.HttpServletRequest;
import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyQueryService surveyQueryService;
    private final SearchService searchService;

    @GetMapping("/survey/login")
    public String loginForm(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "survey/surveyLogin";
    }

    @GetMapping("/survey/queryId={queryId}")
    public String showSurveyQuery(@PathVariable("queryId") int queryId,
                                  Model model) {
        SurveyQuery survey = surveyQueryService.findQuery(queryId);
        String query = survey.getQuery();
        List<BasePostRequest> posts = searchService.findTopPostsSortByReverseTime(query);

        model.addAttribute("posts",posts);

        return "survey/surveyForm";
    }
    // TODO :

    //@GetMapping("/survey/updateQueries")
    public String updateQueries() {
        surveyQueryService.loadQueriesFromFile("static/surveyQueries.txt");
        return "hello";
    }

}
