package knusearch.clear.jpa.repository.post;

import knusearch.clear.jpa.domain.post.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    // 단어로 Term 조회
    Term findByTerm(String term);

    // 특정 단어가 등장한 문서 수 계산
    @Query("SELECT COUNT(DISTINCT p) FROM BasePost p JOIN p.terms t WHERE t.term = :term")
    long countDocumentsWithTerm(@Param("term") String term);
}
