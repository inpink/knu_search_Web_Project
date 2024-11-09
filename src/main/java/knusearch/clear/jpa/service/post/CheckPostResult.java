package knusearch.clear.jpa.service.post;

import java.util.List;
import knusearch.clear.jpa.domain.post.BasePost;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CheckPostResult {
    private final boolean shouldBreak;
    private final List<BasePost> newPosts;

}
