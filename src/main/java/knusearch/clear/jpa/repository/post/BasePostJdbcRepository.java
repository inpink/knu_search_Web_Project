package knusearch.clear.jpa.repository.post;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.post.Term;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BasePostJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(List<BasePost> basePosts) {
        for (BasePost basePost : basePosts) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO base_post " +
                        "(url, site_name, scrt_wrti_yn, encrypted_menu_sequence, encrypted_menu_board_sequence, " +
                        "title, classification, text, image, image_text, date_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

                ps.setString(1, basePost.getUrl());
                ps.setString(2, basePost.getSiteName());
                ps.setBoolean(3, basePost.isScrtWrtiYn());
                ps.setString(4, basePost.getEncryptedMenuSequence());
                ps.setString(5, basePost.getEncryptedMenuBoardSequence());
                ps.setString(6, basePost.getTitle());
                ps.setString(7, basePost.getClassification());
                ps.setString(8, basePost.getText());
                ps.setString(9, basePost.getImage());
                ps.setString(10, basePost.getImageText());
                ps.setDate(11, java.sql.Date.valueOf(basePost.getDateTime()));

                return ps;
            }, keyHolder);

            basePost.setId(keyHolder.getKey().longValue());
        }
    }
}
