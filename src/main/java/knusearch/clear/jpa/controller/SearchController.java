package knusearch.clear.jpa.controller;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.service.ClassificationService;
import knusearch.clear.jpa.service.DateService;
import knusearch.clear.jpa.service.SearchService;
import knusearch.clear.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SearchController {
    private final DateService dateService;
    private final SearchService searchService;
    private final ClassificationService classificationService;

    @GetMapping("/search")
    public String searchForm(Model model) {

        SearchForm searchForm = new SearchForm();

        List<String> selectedSites = searchService.findSites();
        String searchScopeRadio = searchService.findOrder();

        String searchPeriodRadio = searchService.findPeriod();
        LocalDate searchPeriod_start = dateService.minDate();
        LocalDate searchPeriod_end = dateService.currentDate();

        searchForm.setSelectedSites(selectedSites);
        searchForm.setSearchPeriodRadio(searchPeriodRadio);
        searchForm.setSearchPeriod_start(searchPeriod_start);  // value, min, max 값을 모델에 추가
        searchForm.setSearchPeriod_end(searchPeriod_end);
        searchForm.setSearchScopeRadio(searchScopeRadio);
        searchForm.setCategoryRecommendChecked("categoryRecommendChecked");

        model.addAttribute("searchForm", searchForm);

        return "home";
    }

    @GetMapping("/searchResult")
    public String searchResult(@Valid SearchForm searchForm,
                               BindingResult result,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               Model model) {
        if (searchForm.getSelectedSites().isEmpty()) { //사이트 미선택 (List라 isEmpty)
            result.rejectValue("selectedSites", "required");
        }

        if (searchForm.getSearchScopeRadio() == null) { //정렬 순서 미선택 (String이라 null)
            result.rejectValue("searchScopeRadio", "required");
        }

        if (searchForm.getSearchPeriodRadio() == null) { //기간 미선택
            result.rejectValue("searchPeriodRadio", "required");
        }

        if (searchForm.getSearchQuery() == null) { //검색어 미선택
            result.rejectValue("searchQuery", "required");
        }

        for (String site : searchForm.getSelectedSites()) {
            log.info(("선택된 사이트: " + site));
        }
        log.info("검색어: " + searchForm.getSearchQuery());
        log.info("검색 정렬:" + searchForm.getSearchScopeRadio());
        log.info("검색 기간:" + searchForm.getSearchPeriodRadio());
        log.info("검색 기간 시작:" + searchForm.getSearchPeriod_start());
        log.info("검색 기간 끝:" + searchForm.getSearchPeriod_end());
        log.info("분류 추천 사용 여부:" + searchForm.getCategoryRecommendChecked());
        model.addAttribute("searchForm", searchForm);

        // 분류 메뉴 모델로부터 받아오기
        Map<String, Object> predictedAndTokens = classificationService.predictClassification(searchForm.getSearchQuery());
        String predictedClass = (String) predictedAndTokens.get("predictedClass");
        List<String> words = (List<String>) predictedAndTokens.get("words");
        String refinedPredictedClass = StringUtil.deleteLineSeparator(predictedClass);
        log.info("predictedClass = " + predictedClass);
        log.info("refinedPredictedClass = " + refinedPredictedClass);

        // 분류값을 모델에 추가
        model.addAttribute("predictedClass", predictedClass);

        // 검색하기
        Pageable pageable = PageRequest.of(page, size);
        Page<BasePostRequest> searchResult = searchService.searchResultsToPage(
                searchForm.getCategoryRecommendChecked(),
                words,
                searchForm.getSearchQuery(),
                refinedPredictedClass,
                page, size, model);
        model.addAttribute("searchResult", searchResult);

        if (result.hasErrors()) {
            System.out.println("searchForm 검증 과정에서 에러 발생" + result.getAllErrors());
            model.addAttribute("isSearchEnabled", false);
            return "searchResult"; //앞에서 addError 다 해준 뒤 보내주는 것
            //에러 뜨든 안뜨든 searchResult로 보냄. 거기서 다시 검색 옵션 선택하게 함.
        }

        // 끝까지 왔을 때 검색 가능
        model.addAttribute("isSearchEnabled", true);
        return "searchResult"; //redirect 말고 바로 page로 이동시킴. 이유는 아래에
    }

    //위에서 redirect해줬고, 아래 searchResult에서 html 타임리프로 값을 전달하고 싶으면
    //위에서 repository에 저장, 아래에서 repository에서 꺼내씀(service이용) 해야함
}
