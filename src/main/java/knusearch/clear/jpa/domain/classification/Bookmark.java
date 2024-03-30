package knusearch.clear.jpa.domain.classification;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;

import lombok.Getter;

@Getter
@Entity
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int pageNumber;

    private int postId;

    @Temporal(TemporalType.DATE)
    private LocalDate dateTime;
}
