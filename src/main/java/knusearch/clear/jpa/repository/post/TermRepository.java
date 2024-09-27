package knusearch.clear.jpa.repository.post;

import java.util.List;
import knusearch.clear.jpa.domain.post.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    // 단어로 Term 조회
    Term findByName(String name);

    // 특정 단어가 등장한 문서 수 계산
    @Query("SELECT COUNT(DISTINCT pt.basePost) FROM PostTerm pt WHERE pt.term.name = :term")
    long countDocumentsWithTerm(@Param("term") String term);

    List<Term> findByNameIn(List<String> termTexts);

}
