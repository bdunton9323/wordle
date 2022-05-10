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
        // color -> entropy (how well does it split the dictionary)
        // GG -> 0
        // YY -> 0
        // G- -> 1
        // YG -> 0
        // YY -> 0
        // Y- -> 0
        // -G -> 0
        // -Y -> 0
        // -- -> 1
        double reduction = solver.findAverageReduction("an", 4);
        assertThat(reduction).isEqualTo(1.0, offset(.0001d));

        // the only valid colorings that can arise are:
        // Y- -> 0.8112 (splits it 75/25)
        // -- -> 0.8112 (splits it 75/25)
        reduction = solver.findAverageReduction("zp", 4);
        assertThat(reduction).isEqualTo(0.811278124, offset(.0000000005d));

        // TODO: why is this guess scored worse than an? It gives more information
        //       the GG and G- cases both include ab as a match
        // GG -> 0.8112 (75/25 split)
        // G- -> 0.8112 (75/25 split)
        // -- -> 1 (50/50 split)
        reduction = solver.findAverageReduction("ab", 4);
        assertThat(reduction).isEqualTo(0.874185416, offset(.0000000005d));
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
