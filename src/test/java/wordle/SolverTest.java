package wordle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SolverTest {

    @Mock
    private DictionaryFileLoader dictionaryFileLoader;

    @Mock
    private Dictionary dictionary;

    private Solver solver;

    @Before
    public void setUp() throws IOException {
        openMocks(this);
        when(dictionaryFileLoader.buildDictionary()).thenReturn(dictionary);
    }

    // TODO: add a test for single letters. they are easier to predict the calculations.

    @Test
    public void test2Letters() throws IOException {
        withDictionary(List.of("ab", "ac", "xy", "xz"));

        solver = new Solver(2, dictionaryFileLoader);
        // the only colorings that give any info are:
        // G-
        // --
        double reduction = solver.findAverageEntropy("an", new Color[5]);
        assertThat(reduction).isEqualTo(0.28571, offset(.000005d));

        // the only colorings that give any info are:
        // Y-
        // --
        reduction = solver.findAverageEntropy("zp", new Color[5]);
        assertThat(reduction).isEqualTo(0.23179, offset(.000004d));

        // GG
        // G-
        // --
        reduction = solver.findAverageEntropy("ab", new Color[5]);
        assertThat(reduction).isEqualTo(0.37465, offset(.000001d));
    }

    @Test
    public void test5Letters() throws IOException {
        withDictionary(List.of("abcde", "abcdf", "lmnop", "vwxyz"));

        solver = new Solver(5, dictionaryFileLoader);
        double reduction = solver.findAverageEntropy("abcdx", new Color[5]);
        assertThat(reduction).isEqualTo(0.01102, offset(.000001d));
    }

    private void withDictionary(List<String> words) {
        when(dictionary.getWords()).thenReturn(words);
        when(dictionary.size()).thenReturn(words.size());
    }

}
