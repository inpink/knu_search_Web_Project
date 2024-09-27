package knusearch.clear.jpa.repository.post;

import java.util.List;
import knusearch.clear.jpa.domain.post.PostTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTermRepository extends JpaRepository<PostTerm, Long> {

}
