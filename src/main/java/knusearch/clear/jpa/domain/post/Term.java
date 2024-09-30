package knusearch.clear.jpa.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "term")
@Table(indexes = {
    @Index(name = "idx_term", columnList = "name")
}, name = "term")
@Getter
@Setter
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Term termObj = (Term) o;
        return Objects.equals(name, termObj.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
