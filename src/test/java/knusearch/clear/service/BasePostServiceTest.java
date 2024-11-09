package knusearch.clear.service;

import static org.junit.Assert.assertEquals;

import knusearch.clear.jpa.domain.post.BasePost;
import knusearch.clear.jpa.service.ScrapingService;
import knusearch.clear.jpa.service.post.BasePostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasePostServiceTest {

    @Mock
    private ScrapingService scrapingService;

    @InjectMocks
    private BasePostService basePostService;

    @Test
    public void updateClassification() {
        // Given
        String newClassification = "someClassification";
        BasePost basePost = new BasePost(); // Assuming a default constructor is available
        basePost.setClassification("oldClassification");

        // When
        basePostService.updateClassification(basePost, newClassification);

        // Then
        assertEquals(newClassification, basePost.getClassification());
    }
}
