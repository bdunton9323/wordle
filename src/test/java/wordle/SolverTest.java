package wordle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class SolverTest {

    @Mock
    private Dictionary dictionary;

    @Mock
    private WordMatcher matcher;

    @Mock
    private GoodnessCalculator goodnessCalculator;

    private Solver solver;

    @Before
    public void setUp() {
        openMocks(this);
        solver = new Solver(5, dictionary, matcher, goodnessCalculator);
    }

    @Test
    public void testFindFirstWord() {
        withDictionary("excel", "picks", "stamp", "tramp");
        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        when(goodnessCalculator.calculateGoodness(wordCaptor.capture(), any(Color[].class), eq(4)))
                .thenReturn(0.1)
                .thenReturn(0.9)
                .thenReturn(0.2)
                .thenReturn(0.3)
                .thenReturn(0.4);

        assertThat(solver.findFirstWord()).isEqualTo("picks");

        assertThat(wordCaptor.getAllValues()).containsExactly("excel", "picks", "stamp", "tramp");
    }

    @Test
    public void testFindNextWordFromPreviousGuess() {
        withDictionary("excel", "picks", "stamp", "tramp");
        String previousGuess = "squat";
        Color[] previousOutcome = new Color[]{ Color.YELLOW, Color.GRAY, Color.GRAY, Color.GRAY, Color.YELLOW };

        Set<String> newMatches = Set.of("picks", "stamp");
        when(matcher.getMatchingWords(previousGuess.toCharArray(), previousOutcome)).thenReturn(newMatches);

        ArgumentCaptor<String> wordCaptor = ArgumentCaptor.forClass(String.class);
        when(goodnessCalculator.calculateGoodness(wordCaptor.capture(), same(previousOutcome), eq(4)))
                .thenReturn(0.1)
                .thenReturn(0.9)
                .thenReturn(0.2)
                .thenReturn(0.3)
                .thenReturn(0.4);

        assertThat(solver.findNextWord(previousGuess, previousOutcome)).isEqualTo("picks");

        assertThat(wordCaptor.getAllValues()).containsExactly("excel", "picks", "stamp", "tramp");
        verify(dictionary).intersect(newMatches);
    }

    @Test
    public void testFindNextWordOneLeft() {
        withDictionary("pints");
        assertThat(solver.findNextWord("dance", new Color[]{})).isEqualTo("pints");
        verifyNoInteractions(goodnessCalculator);
    }

    private void withDictionary(String... words) {
        when(dictionary.size()).thenReturn(words.length);
        when(dictionary.getWords()).thenReturn(Arrays.asList(words));
    }
}
