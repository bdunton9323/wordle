package wordle;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverIntegrationTest {

    private Solver solver;

    @Before
    public void setUp() {
        solver = new Solver(5, new DictionaryFileLoader("/dictionary.txt"));
    }

    @Test
    public void sanityCheck() {
        // as a wordle player, I have an intuition as to which of these words is best. If the algorithm breaks that
        // ordering, then something surely went wrong.
        double tares = solver.findAverageEntropy("tares", new Color[5]);
        double guest = solver.findAverageEntropy("guest", new Color[5]);
        double pizza = solver.findAverageEntropy("pizza", new Color[5]);
        double fuzzy = solver.findAverageEntropy("fuzzy", new Color[5]);

        List<Double> sorted = Stream.of(tares, guest, pizza, fuzzy)
                .sorted().collect(Collectors.toList());
        assertThat(sorted).containsExactly(fuzzy, pizza, guest, tares);
    }
}
