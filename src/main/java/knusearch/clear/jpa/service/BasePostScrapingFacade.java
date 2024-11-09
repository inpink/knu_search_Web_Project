package knusearch.clear.jpa.service;

import java.util.List;
import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.domain.site.Board;
import knusearch.clear.jpa.domain.site.Site;
import knusearch.clear.jpa.repository.post.BasePostJdbcRepository;
import knusearch.clear.jpa.repository.post.BasePostRepository;
import knusearch.clear.jpa.service.post.BasePostService;
import knusearch.clear.jpa.service.post.CheckPostResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasePostScrapingFacade {

    private final ScrapingService scrapingService;
    private final BasePostService basePostService;
    private final BasePostRepository basePostRepository;
    private final BasePostJdbcRepository basePostJdbcRepository;

    @Transactional
    public void crawlUpdate() { // crawl and make baseposts
        String baseUrl = basePostService.getBaseUrl();
        List<Board> boards = basePostService.getBoards();

        for (Board board : boards) {
            String postUrl = board.getEncryptedName();
            String firsNoticetUrl = scrapingService.makeFinalPostListUrl(baseUrl, postUrl, 1);
            int totalPageIdx = scrapingService.totalPageIdx(firsNoticetUrl); //총 페이지수 구해옴

            for (int i = 1; i <= totalPageIdx; i++) {
                //for (int i = 1; i <= 2; i++) { //너무 많으니까 일단 10개정도로 테스트

                //굳이 안받아와도 되긴할듯 필요하면 받아오고 //상속관계를 이용하여 BaseContent로 통일!
                //추상화를 통해 DIP(의존관계역전) 적용된 케이스임
                //List<BasePost> contentList = scrapeWebPage(baseUrl, postUrl ,i); //10페이지에 있는 것 contentMain에 저장시킴?
                Elements links = scrapingService.getAllLinksFromOnePage(baseUrl, postUrl, i);

                for (Element linkElement : links) {
                    BasePost basePost = scrapingService.setURLValues(linkElement, baseUrl, postUrl);

                    checkAndSave(basePost);
                }
                log.info(i + "번째 페이지에 있는 모든 게시글 크롤링");
            }
        }
    }

    //TODO: 현재는 이미 추가된 게시글이면 update 안함. 추후, 본문이 수정된 경우나 글이 삭제된 경우도 커버할 수 있게끔 구현할 수 있음.
    // : 전체 삭제하고 다시 업로드(자원이 많이 들긴 하겠다만). 트랜잭션에 의해 재업하는 동안에도 문제없음. "테이블 행 싹 비우는 코드 java에서 하고 -> 모두 다시 업로드" 하는 방식 이용
    @Transactional
    public void checkAndSave(BasePost basePost) {
        String encMenuSeq = basePost.getEncryptedMenuSequence();
        String encMenuBoardSeq = basePost.getEncryptedMenuBoardSequence();

        //DB에 없는 것만 추가!!!
        if (basePostRepository.findAllByEncryptedMenuSequenceAndEncryptedMenuBoardSequence(
            encMenuSeq, encMenuBoardSeq).size() == 0) {
            scrapingService.setPostValues(basePost);
            // 추출한 데이터를 MySQL 데이터베이스에 저장하는 코드 추가
            basePostRepository.save(basePost); //★
        }
    }

    public void scrapeYesterdayPosts(Site site) {
        String baseUrl = site.getBaseUrl();
        List<Board> boards = site.getBoards();

        for (Board board : boards) {
            String postUrl = board.getEncryptedName();
            savePostsWithinPeriod(baseUrl, postUrl);
        }
    }

    @Transactional
    public void savePostsWithinPeriod(String baseUrl, String postUrl) {
        int pageIdx = 1;
        boolean isTimeToBreak = false;

        while (!isTimeToBreak) {
            Elements links = scrapingService.getAllLinksFromOnePage(baseUrl, postUrl, pageIdx);
            CheckPostResult checkPostResult = scrapingService.checkWithinPeriod(baseUrl, postUrl, links);
            isTimeToBreak = checkPostResult.isShouldBreak();
            List<BasePost> linkedPosts = checkPostResult.getNewPosts();
            basePostJdbcRepository.saveAll(linkedPosts);
            basePostService.updateIndex(linkedPosts);
            pageIdx++;
        }
    }
}
