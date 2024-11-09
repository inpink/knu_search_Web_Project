package knusearch.clear.jpa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassificationService {

    private final RestTemplate restTemplate;
    private final Map<String, String> classificationTransform = new HashMap<>() {{
        put("0","학사");
        put("1","장학");
        put("2","학습/상담");
        put("3","취창업");
    }};

    @Transactional
    public Map<String, Object> predictClassification(String query) {
        query = query.replaceAll("[^가-힣\\s]", "");  // 한글과 공백을 제외한 모든 문자를 제거
        if (query.isEmpty()) {
            throw new IllegalArgumentException("Query is empty");
        }

        // Flask 서버에 요청을 보내기 위한 데이터 구성
        String flaskEndpoint = "http://13.209.132.169:5000/predict"; // Flask 서버의 URL
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", query);

        // Flask 서버로 POST 요청을 보내고 응답 받기
        ResponseEntity<String> response = restTemplate.postForEntity(flaskEndpoint, requestBody, String.class);
        String body = response.getBody();

        // 응답에서 분류값과 단어 리스트 추출
        JSONObject jsonResponse = new JSONObject(body);
        String predictedClass = jsonResponse.getString("predicted_class"); // "predicted_class" 키의 값을 int로 추출
        JSONArray wordsArray = jsonResponse.getJSONArray("words"); // "words" 키의 값으로 JSONArray 받기
        List<String> words = new ArrayList<>(); // JSONArray를 List<String>으로 변환

        for (int i = 0; i < wordsArray.length(); i++) {
            words.add(wordsArray.getString(i)); // 각 요소를 String으로 변환하여 리스트에 추가
        }

        // 결과를 Map에 담아 반환
        Map<String, Object> result = new HashMap<>();
        result.put("predictedClass", predictedClass);
        result.put("words", words);

        return result;
    }


    public String findClassification(String classification) {
        return classificationTransform.get(classification);
    }
}
