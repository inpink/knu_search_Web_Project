package knusearch.clear.jpa.domain.classification;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum Classification {

    ACADEMIC_NOTIFICATION("학사", 0),
    SCHOLARSHIP("장학", 1),
    LEARNING_KNOWHOW("학습/상담", 2),
    EMPLOYMENT_STARTUP("취창업", 3);

    private final String description;

    private final int index;

    Classification(String description, int index) {
        this.description = description;
        this.index = index;
    }

    public static int calcMinIndex() {
        return getIndexes()
                .min()
                .orElseThrow(() -> new NoSuchElementException("No enum constants in Classification"));
    }

    public static int calcMaxIndex() {
        return getIndexes()
                .max()
                .orElseThrow(() -> new NoSuchElementException("No enum constants in Classification"));
    }

    public static int findIndex(String description) {
        return Arrays.stream(Classification.values())
                .filter(clas -> clas.description.equals(description))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("No enum constants in Classification"))
                .getIndex();
    }

    public static String findDescription(String index) {
        return Arrays.stream(Classification.values())
                .filter(clas -> String.valueOf(clas.index).equals(index))
                .findAny()
                .orElseThrow(() -> new NoSuchElementException("No enum constants in Classification"))
                .getDescription();
    }

    private static IntStream getIndexes() {
        return Arrays.stream(Classification.values())
                .mapToInt(Classification::getIndex);
    }

    private static Stream<String> getDescriptions() {
        return Arrays.stream(Classification.values())
                .map(Classification::getDescription);
    }
}
