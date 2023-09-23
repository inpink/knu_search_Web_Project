package knusearch.clear.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class SearchForm { //form 데이터를 bindingResult로 받아올때 쓸 객체
    //DTO (Data Transfer Object) : Client, Service, Controller, Repsitory 사이에서 데이터를 전달하기 위해 쓰임
    //getter/setter 만

    //@NotEmpty(message = "하나 이상의 검색 사이트를 선택해주세요.")
    private List<String> selectedSites;

    //@NotEmpty(message = "검색 결과 정렬 순서를 선택해주세요.")
    private String searchScopeRadio;

    //@NotEmpty(message = "검색 범위를 선택해주세요.")
    private String searchPeriodRadio;

    //form에서는 String형으로 보내줌. @DateTimeFormat 어노테이션을 사용하여 Date형으로 편하게 변환
    //@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) => ISO 8601 형식은  Sat Sep 23 09:00:00 KST 2023형태로
    @DateTimeFormat(pattern = "yyyy-MM-dd") //나는 지금은 연,월,일만 필요하다.
    private Date searchPeriod_start;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date searchPeriod_end;

    private String searchQuery;


}

//html form에서 보낸 데이터는 구글 F12 Payload에서 body로 확인할 수 있다.