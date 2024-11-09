package knusearch.clear.jpa.service.post;

import knusearch.clear.jpa.domain.site.Site;
import knusearch.clear.jpa.service.ScrapingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostScrapingTask {

    private final ScrapingService scrapingService;

    public PostScrapingTask(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void performTask() {
        scrapingService.scrapeYesterdayPosts(Site.MAIN);
    }
}
