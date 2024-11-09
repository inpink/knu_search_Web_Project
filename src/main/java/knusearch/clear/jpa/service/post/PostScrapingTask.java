package knusearch.clear.jpa.service.post;

import knusearch.clear.jpa.domain.site.Site;
import knusearch.clear.jpa.service.BasePostScrapingFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostScrapingTask {

    private final BasePostScrapingFacade basePostScrapingFacade;

    @Scheduled(cron = "0 0 0 * * *")
    public void performTask() {
        basePostScrapingFacade.scrapeYesterdayPosts(Site.MAIN);
    }
}
