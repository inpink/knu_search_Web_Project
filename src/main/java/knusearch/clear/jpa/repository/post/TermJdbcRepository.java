package knusearch.clear.jpa.repository.post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import knusearch.clear.jpa.domain.post.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TermJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<Term> terms) {
        for (Term term : terms) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO TERM (`TERM`) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, term.getName());
                return ps;
            }, keyHolder);

            // 삽입된 ID를 Term 객체에 설정
            term.setId(keyHolder.getKey().longValue());
        }
    }
}
