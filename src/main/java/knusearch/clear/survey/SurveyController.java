package knusearch.clear.survey;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyQueryService surveyQueryService;

    @GetMapping("/survey/login")
    public String loginForm(Model model, HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "survey/surveyLogin";
    }

    @PostMapping("/survey/login")
    public String loginSubmit(@RequestParam("username") String username,
                              @RequestParam("password") String password) {
        log.info("Username: " + username);
        log.info("Password: " + password);



        return "redirect:/survey/queryNumber=1";
    }

    @GetMapping("/survey/queryNumber={number}")
    public String handleSurveyQuery(@PathVariable("number") int number) {

        return "hello";
    }


    @GetMapping("/survey/updateQueries")
    public String  updateQueries() {
        surveyQueryService.loadQueriesFromFile("static/surveyQueries.txt");

        return "hello";
    }

}
