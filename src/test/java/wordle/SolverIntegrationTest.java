package wordle;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverIntegrationTest {

    private GoodnessCalculator calculator;
    private int dictSize;

    @Before
    public void setUp() throws IOException {
        DictionaryFileLoader dictLoader = new DictionaryFileLoader("/dictionary.txt");
        Dictionary dict = dictLoader.buildDictionary();
        dictSize = dict.size();
        calculator = new GoodnessCalculator(5, new WordMatcher(dict), new EntropyCalculator());
    }

    @Test
    public void sanityCheck() {
        // as a wordle player, I have an intuition as to which of these words is best. If the algorithm breaks that
        // ordering, then something surely went wrong.
        double tares = calculator.calculateGoodness("tares", new Color[5], dictSize);
        double guest = calculator.calculateGoodness("guest", new Color[5], dictSize);
        double pizza = calculator.calculateGoodness("pizza", new Color[5], dictSize);
        double fuzzy = calculator.calculateGoodness("fuzzy", new Color[5], dictSize);

        List<Double> sorted = Stream.of(tares, guest, pizza, fuzzy)
                .sorted().collect(Collectors.toList());
        assertThat(sorted).containsExactly(fuzzy, pizza, guest, tares);
    }
}
