package knusearch.clear.jpa.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;

import knusearch.clear.constants.StringConstants;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "base_post")
@Getter
@Setter
public class BasePost {

    public static final int TEXT_COLUMN_LENGTH = 40000;
    public static final int IMAGE_COLUMN_LENGTH = 8000;

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

    public String getContent() {
        return this.text + this.imageText;
    }
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
