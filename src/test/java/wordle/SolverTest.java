package wordle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class SolverTest {

    @Mock
    private IndexBuilder indexBuilder;

    @Mock
    private WordIndex wordIndex;

    private Solver solver;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @Test
    public void test2Letters() throws IOException {
        when(wordIndex.getDictionary()).thenReturn(List.of("ab", "ac", "xy", "xz"));
        when(wordIndex.getDictionaryCopy()).thenReturn(new HashSet<>(Arrays.asList("ab", "ac", "xy", "xz")));

        when(indexBuilder.buildIndex(2)).thenReturn(wordIndex);
        solver = new Solver(2, indexBuilder);
        // the only colorings that give any info are:
        // G-
        // --
        double reduction = solver.findAverageEntropy("an", 4);
        assertThat(reduction).isEqualTo(0.28571, offset(.000005d));

        // the only colorings that give any info are:
        // Y-
        // --
        reduction = solver.findAverageEntropy("zp", 4);
        assertThat(reduction).isEqualTo(0.23179, offset(.000004d));

        // GG
        // G-
        // --
        reduction = solver.findAverageEntropy("ab", 4);
        assertThat(reduction).isEqualTo(0.37465, offset(.000001d));
    }

    @Test
    public void test5Letters() throws IOException {
        when(wordIndex.getDictionary()).thenReturn(List.of("abcde", "abcdf", "lmnop", "vwxyz"));
        when(wordIndex.getDictionaryCopy()).thenReturn(new HashSet<>(Arrays.asList("abcde", "abcdf", "lmnop", "vwxyz")));

        when(indexBuilder.buildIndex(5)).thenReturn(wordIndex);
        solver = new Solver(5, indexBuilder);
        double reduction = solver.findAverageEntropy("abcdx", 4);
        assertThat(reduction).isEqualTo(0.01102, offset(.000001d));
    }

}
