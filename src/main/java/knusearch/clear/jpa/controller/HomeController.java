package knusearch.clear.jpa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        log.info("home controller");

        // "/" 경로에서 "/search" 경로로 리다이렉트
        return "redirect:/search";
    }


    //@GetMapping("/test")
    public String test() {
        return "recommendSearchQuery";
    }

    //@PostMapping("/searchRecommend")
    @ResponseBody
    public List<String> search(@RequestBody String query) {

        // 랜덤 객체 생성
        Random random = new Random();

        // 원하는 범위 내에서 랜덤 정수 생성 (예: 1부터 100까지의 범위)
        int min = 1;
        int max = 100;
        int randomNumber = random.nextInt(min, max);

        // 검색 로직을 수행하고 결과를 반환합니다.
        List<String> results = new ArrayList<>();
        results.add("검색 결과" + randomNumber);
        results.add("검색 결과" + randomNumber);
        results.add("검색 결과" + randomNumber);

        return results;
    }

    // PostMapping에서 파라미터로 xxxx-xx-xx형식으로 오는 데이터를 MemberDTO memberDTO에 받을 때,
    // MemberDTO의 field에  @DateTimeFormat(pattern = "yyyy-MM-dd") private Date birthDate;처럼
    //@DateTimeFormat을 달아서 원하는 형식에 맞게 복잡한 파싱 없이 Date 객체에 담아둘수 있음
    //https://velog.io/@chb1828/Controller%EC%97%90%EC%84%9C-Date-%EA%B0%9D%EC%B2%B4%EB%A5%BC-%EB%B0%9B%EB%8A%94-%EB%B0%A9%EB%B2%95 참고해보기
}
