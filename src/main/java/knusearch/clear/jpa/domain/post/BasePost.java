package knusearch.clear.jpa.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;

import knusearch.clear.constants.StringConstants;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BasePost {
    /* 굳이 추상클래스로 두지말자(엔티티는 특정 비지니스 로직을 담고있기 때문에 조금만 달라져도 재사용이 어려움
      https://www.inflearn.com/questions/63567/%EC%95%88%EB%85%95%ED%95%98%EC%8B%AD%EB%8B%88%EA%B9%8C-%EB%8F%84%EB%A9%94%EC%9D%B8-%EC%84%A4%EA%B3%84%EC%8B%9C-%EC%9D%B8%ED%84%B0%ED%8E%98%EC%9D%B4%EC%8A%A4-%EC%B6%94%EC%83%81%ED%81%B4%EB%9E%98%EC%8A%A4-%EC%97%90-%EB%8C%80%ED%95%9C-%EC%9D%98%EA%B2%AC%EC%9D%84-%EB%93%A3%EA%B3%A0-%EC%8B%B6%EC%8A%B5%EB%8B%88%EB%8B%A4
        직접 사용해보니 굳이 PostMain이랑 PostIct의 구분을 둘만한 지점이 없었음. 오히려 이들을 분리함으로서 복잡해지기만 함.
*/

    //게시글 테이블에 공통적으로 쓰이는 필드를 담음(추상화)  //많은 경우 추상화를 통해 의존관계 역전을 달성하는 것이 일반적
    //URL3종(scrtWrtiYn, encMenuSeq,encMenuBoardSeq) 제목, 본문, 이미지 링크(여러개 할거면 또 테이블 필요함), 날짜, 사이트 번호
    //추상클래스!
    //자바에서 abstract class와 default interface 둘 다를 쓸 수 있으면 후자를 선택하는 이유
    // => 대표적으로 다중 상속 가능, API 디자인 가능
    // => 생성자가 꼭 필요한 경우에는 abstract class 사용할 것
    // => 하지만 Effective Java에서 생성자 조차 "정적 팩터리 메서드"로 대체하는 것을 추천한다고 했음
    // => 하지만 여기서는 private field가 필요하므로 abstract class를 썼다
    //결론 : 상황에 맞게 선택할 것

    public static final int TEXT_COLUMN_LENGTH = 40000;
    public static final int IMAGE_COLUMN_LENGTH = 8000; //외부에서도 사용하고 쉽게 뜻을 알 수 있게 static final

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String url;

    private String siteName;

    private boolean scrtWrtiYn;

    private String encryptedMenuSequence;

    private String encryptedMenuBoardSequence;

    private String title;

    private String classification;

    //JPA에서 String을 주면 기본적으로 255Byte varchar이 된다.
    // UTF-8 인코딩을 기준으로 255바이트로는 약 63~85글자의 한글을 담을 수 있음. 한글자에3~4byte이기 때문
    //열의 크기를 지나치게 크게 설정하면 데이터베이스 디스크 공간을 낭비하게 된다.
    // 모든 데이터를 담으려면 분할하거나, 저장공간 많이 쓰거나 고민이 필요
    @Column(length = TEXT_COLUMN_LENGTH)
    private String text;

    @Column(length = IMAGE_COLUMN_LENGTH)
    private String image;  //일단 1개만 담게. 여러개 할거면 또 image 테이블 필요함. 1대 다 구조

    @Column(length = TEXT_COLUMN_LENGTH)
    private String imageText;

    //만약 시간 정보를 함께 저장하려면 LocalDate말고 java.time.LocalDateTime을 사용하고 TemporalType.TIMESTAMP로 매핑
    //유형 일치 중요
    @Temporal(TemporalType.DATE)
    private LocalDate dateTime;


    //==생성 메서드==//
    public static BasePost createBasePost(String site, String url,
                                          boolean scrtWrtiYn, String encMenuSeq, String encMenuBoardSeq,
                                          String title, String text, String image, LocalDate dateTime) {
        BasePost basePost = new BasePost();

        basePost.setSiteName(site);
        basePost.setUrl(url);
        basePost.setScrtWrtiYn(scrtWrtiYn);
        basePost.setEncryptedMenuSequence(encMenuSeq);
        basePost.setEncryptedMenuBoardSequence(encMenuBoardSeq);
        basePost.setTitle(title);
        basePost.setText(text);
        basePost.setImage(image);
        basePost.setDateTime(dateTime);
        basePost.setClassification(StringConstants.UNDETERMINED.getDescription());
        return basePost;
    }
}
