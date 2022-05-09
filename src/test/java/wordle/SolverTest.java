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
        // gg -> 0
        // gy -> 0
        // g- -> 0.5
        // yg -> 0
        // yy -> 0
        // y- -> 0
        // -g -> 0
        // -y -> 0
        // -- -> 0.5
        double reduction = solver.findAverageReduction("an", 4);
        assertThat(reduction).isEqualTo(0.1111, offset(.00002d));
    }

    @Test
    public void test5Letters() throws IOException {
        when(wordIndex.getDictionary()).thenReturn(List.of("abcde", "abcdf", "lmnop", "vwxyz"));
        when(wordIndex.getDictionaryCopy()).thenReturn(new HashSet<>(Arrays.asList("abcde", "abcdf", "lmnop", "vwxyz")));

        when(indexBuilder.buildIndex(5)).thenReturn(wordIndex);
        solver = new Solver(5, indexBuilder);
        double reduction = solver.findAverageReduction("abcdx", 4);
        assertThat(reduction).isEqualTo(0.5, offset(.01d));
    }
}
