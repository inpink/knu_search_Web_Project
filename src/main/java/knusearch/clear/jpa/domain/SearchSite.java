package knusearch.clear.jpa.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "search_site")
@Getter
@Setter
public class SearchSite { //검색 옵션에서 사이트는 여러개 선택할 수 있다.
    // 사용자가 한 번 검색할때 선택한 사이트들을 이 테이블에 담음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "search_site_id")
    private Long id;

    private String name;

    /*
    fetch = FetchType.LAZY: 이 부분은 엔티티를 로딩하는 방식을 설정하는 것입니다. FetchType.LAZY는 지연 로딩(Lazy Loading)을 나타냅니다.
    이 옵션을 선택하면 연관된 엔티티를 실제로 접근할 때까지 로딩을 지연시킵니다.
    즉, 엔티티를 조회할 때 연관 엔티티는 로딩되지 않고,
    실제로 해당 엔티티의 필드나 메소드를 사용할 때 비로소 로딩이 발생합니다.
    이러한 설정은 데이터베이스 쿼리를 최적화하고 애플리케이션의 성능을 향상시키는 데 도움을 줄 수 있습니다.
    예를 들어, 주문(Order) 엔티티가 @ManyToOne 관계로 상품(Product) 엔티티를 참조한다고 가정해보겠습니다.
    만약 fetch = FetchType.LAZY로 설정되어 있다면,
    주문 엔티티를 조회할 때는 해당 주문과 연관된 상품 정보는 로딩되지 않습니다.
    그러나 실제로 주문의 상품 정보를 조회하려고 할 때 비로소 로딩이 발생합니다.
    특히, 연관된 엔티티가 많은 경우에는 지연 로딩을 사용하면 필요한 데이터만 로딩하여 불필요한 데이터를 조회하는 것을 방지할 수 있습니다.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) //1(search) : 다(searchsite)
    @JoinColumn(name = "search_id")
    private Search search;


    //==생성 메서드==//
    public static SearchSite createSearchSite(String name) { //마찬가지로 SearchService에서 갖다 쓸 예정
        SearchSite searchSite = new SearchSite();
        searchSite.setName(name);

        return searchSite;
    }


}
