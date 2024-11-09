package knusearch.clear;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import knusearch.clear.jpa.domain.Search;
import knusearch.clear.jpa.domain.SearchSite;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.service.ScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    @PostConstruct
    public void init() {
        /*initService.dbInit1();
        initService.dbInit2();*/
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final EntityManager em;
        private final ScrapingService scrapingService;


        public void dbInit1() {
            System.out.println("Init1" + this.getClass());

            SearchSite searchSite1 = SearchSite.createSearchSite("searchSite1");
            SearchSite searchSite2 = SearchSite.createSearchSite("searchSite2");
            Search search = Search.createSearch(searchSite1, searchSite2);
            em.persist(search); //★여기는 초기 예시라 그렇고, 실제는 클라이언트에 의해 동적으로 되므로,
            //DB는 repository를 이용해 조작한다.

            LocalDate localDate = LocalDate.of(2023, 9, 26);
            BasePost basePost = BasePost.createBasePost(
                    "MainBoard",
                    "https://web.kangnam.ac.kr/menu/board/info/f19069e6134f8f8aa7f689a4a675e66f.do?encMenuSeq=c5dc4b1d7b4dd402e5e6a7a8471eb55c&encMenuBoardSeq=fd44199d361453b9a13e93e28ff46ac0",
                    false, "c5dc4b1d7b4dd402e5e6a7a8471eb55c", "fd44199d361453b9a13e93e28ff46ac0",
                    "[세무] 2024-1학기 세무학전공(주.야) 강의시간표 변경 안내", "본문임 ", "imageLink~~", localDate);
            em.persist(basePost);


            /*Member member = createMember("userA", "서울", "1", "1111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);*/
        }


        public void dbInit2() {
            System.out.println("Init2" + this.getClass());

            SearchSite searchSite3 = SearchSite.createSearchSite("searchSite3");
            SearchSite searchSite4 = SearchSite.createSearchSite("searchSite4");
            Search search = Search.createSearch(searchSite3, searchSite4);
            em.persist(search);

            /*Member member = createMember("userA", "서울", "1", "1111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK", 10000, 100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK", 20000, 100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);*/
        }


    }
}

