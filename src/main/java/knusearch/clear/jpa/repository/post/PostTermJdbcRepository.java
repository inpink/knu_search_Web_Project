package knusearch.clear.jpa.repository.post;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import knusearch.clear.jpa.domain.post.PostTerm;
import knusearch.clear.jpa.domain.post.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostTermJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private int batchSize = 500;

    public void saveAll(List<PostTerm> terms) {
        int batchCount = 0;
        List<PostTerm> subTerms = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            subTerms.add(terms.get(i));
            if ((i + 1) % batchSize == 0) {
                batchCount = batchInsert(batchSize, batchCount, subTerms);
            }
        }
        if (!subTerms.isEmpty()) {
            batchCount = batchInsert(batchSize, batchCount, subTerms);
        }
    }

    private int batchInsert(int batchSize, int batchCount, List<PostTerm> subTerms) {
        jdbcTemplate.batchUpdate("INSERT INTO POST_TERM (post_id, term_id) VALUES (?, ?)",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, subTerms.get(i).getBasePost().getId()); // basePost의 ID
                    ps.setLong(2, subTerms.get(i).getTerm().getId());     // term의 ID
                }

                @Override
                public int getBatchSize() {
                    return subTerms.size();
                }
            });
        subTerms.clear();
        batchCount++;
        return batchCount;
    }
}
