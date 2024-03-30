package knusearch.clear.jpa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Search { //bindingResult로 받은 SearchForm객체에서 정제해서 이 테이블에 저장
    //Entity : 실제 DB의 테이블과 매칭되는 클래스
    //Entity에서 구현한 메소드는 주로 Service에서 사용한다

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_id")
    private Long id;

    /*
    @JsonIgnore를 붙이면 해당 필드의 값은 JSON으로 변환될 때 포함되지 않습니다.

    CascadeType.ALL은 모든 변경 작업(추가, 수정, 삭제 등)이 부모 엔티티에 적용될 때 자식 엔티티에도 적용되도록 설정합니다.
     즉, search 엔티티를 저장할 때 관련된 모든 searchSite 엔티티도 자동으로 저장됩니다.

     mappedBy : 양방향 매핑에서, search 객체와 searchsite 객체가 모두 db의 SEARCH table에 접근할 수 있어진다.
     무결성을 해칠 수 있으므로 두 객체 중 하나의 객체만 SEARCH table에 접근할 수 있게 해주자
     */
    @JsonIgnore
    @OneToMany(mappedBy = "search", cascade = CascadeType.ALL)  //1(search) : 다(searchsite)
    private List<SearchSite> searchSites = new ArrayList<>();
    //RDB 테이블에 list가 들어갈 수 없으므로,


    //==연관관계 메서드==//
    public void addSearchSite(SearchSite searchSite) {
        searchSites.add(searchSite);
        searchSite.setSearch(this);
    }


    //==생성 메서드==//
    public static Search createSearch(SearchSite... searchSites) { //SearchService에서 갖다 쓸 예정
        Search search = new Search();

        for (SearchSite searchSite : searchSites) {
            search.addSearchSite(searchSite);
        }

        return search;
    }

}
