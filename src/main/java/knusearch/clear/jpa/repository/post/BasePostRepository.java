package knusearch.clear.jpa.repository.post;

import java.util.List;

import knusearch.clear.jpa.domain.dto.BasePostRequest;
import knusearch.clear.jpa.domain.post.BasePost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BasePostRepository extends JpaRepository<BasePost, Long> {

    // 전체 문서 수 계산
    @Query("SELECT COUNT(p) FROM BasePost p")
    long countTotalPosts();

    List<BasePost> findAllByEncryptedMenuSequenceAndEncryptedMenuBoardSequence(
            String EncryptedMenuSequence,
            String EncryptedMenuBoardSequence);

    List<BasePost> findAllByTitleContaining(String query);

    List<BasePost> findAllByTextContaining(String query);

    @Query("SELECT new knusearch.clear.jpa.domain.dto.BasePostRequest(bp.id, bp.url, bp.title," +
            "bp.text, bp.image, bp.dateTime, bp.classification) " +
            "FROM BasePost bp WHERE bp.title LIKE %:titleQuery% OR bp.text LIKE %:textQuery%")
    List<BasePostRequest> findByTitleOrTextQuery(@Param("titleQuery") String titleQuery, @Param("textQuery") String textQuery);

    @Query("SELECT bp FROM BasePost bp WHERE bp.title LIKE %:query% AND bp.title NOT LIKE %:except%")
    List<BasePost> findByTitleQueryExcept(@Param("query") String query, @Param("except") String except);

    @Query("SELECT bp FROM BasePost bp WHERE bp.text LIKE %:query% AND bp.text NOT LIKE %:except%")
    List<BasePost> findByTextQueryExcept(@Param("query") String query, @Param("except") String except);

    @Query("SELECT bp FROM BasePost bp WHERE (bp.title LIKE %:titleQuery% OR bp.text LIKE %:textQuery%) AND bp.title NOT LIKE %:except%")
    List<BasePost> findByTitleOrTextQueryExcept(@Param("titleQuery") String titleQuery,
                                                @Param("textQuery") String textQuery,
                                                @Param("except") String except);
    //메서드 이름이 너무 길어져서 @Query 사용

    @Query("SELECT bp FROM BasePost bp WHERE bp.classification NOT IN (:classifications)")
    List<BasePost> findBasePostsNotInClassifications(@Param("classifications") List<String> classifications,
                                                     Pageable pageable);


    // 모든 'title'과 'text'를 연결하여 가져오는 쿼리
    @Query("SELECT CONCAT(p.title, ' ', p.text) FROM BasePost p")
    List<String> findAllTitlesAndTexts();
}
