package wordle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class GoodnessCalculatorTest {

    private static final int NUM_MATCHING = 10;
    private static final int NUM_CHOICES = 100;

    @Mock
    private WordMatcher matcher;

    @Mock
    private EntropyCalculator entropyCalculator;

    @Before
    public void setUp() {
        openMocks(this);
    }


    @Test
    public void testAllSameProbability() {

        when(matcher.countMatchingWords(any(), any())).thenReturn(NUM_MATCHING);
        when(entropyCalculator.calculateEntropy(NUM_MATCHING, NUM_CHOICES)).thenReturn(0.5);

        GoodnessCalculator calculator = new GoodnessCalculator(2, matcher, entropyCalculator);
        double goodness = calculator.calculateGoodness("ab", new Color[2], NUM_CHOICES);

        // 2 letters results in 8 possible color combos, 1 of which is invalid, so that's (0.5*7)/7
        assertThat(goodness).isEqualTo(0.5);
    }

    @Test
    public void testAveraging() {
        when(matcher.countMatchingWords(any(), any())).thenReturn(NUM_MATCHING);
        when(entropyCalculator.calculateEntropy(anyInt(), anyInt()))
                .thenReturn(0.1)
                .thenReturn(0.2)
                .thenReturn(0.3)
                .thenReturn(0.4)
                .thenReturn(0.5)
                .thenReturn(0.6)
                .thenReturn(0.7);

        GoodnessCalculator calculator = new GoodnessCalculator(2, matcher, entropyCalculator);
        double goodness = calculator.calculateGoodness("ab", new Color[2], 100);

        // 2 letters results in 8 possible color combos, 1 of which is invalid, so that's (0.5*7)/7
        assertThat(goodness).isEqualTo(0.4, offset(0.0009));

    }

    @Test
    public void test5LetterCorrectCombinationCount() {
        when(matcher.countMatchingWords(any(), any())).thenReturn(NUM_MATCHING);
        when(entropyCalculator.calculateEntropy(anyInt(), anyInt())).thenReturn(0.5);

        GoodnessCalculator calculator = new GoodnessCalculator(5, matcher, entropyCalculator);
        calculator.calculateGoodness("abcde", new Color[5], NUM_CHOICES);
        // 3^5 minus some invalid ones
        verify(entropyCalculator, times(238)).calculateEntropy(NUM_MATCHING, NUM_CHOICES);
    }

    @Test
    public void shouldSkipKnownLetters() {
        when(matcher.countMatchingWords(any(), any())).thenReturn(NUM_MATCHING);
        when(entropyCalculator.calculateEntropy(anyInt(), anyInt())).thenReturn(0.5);

        GoodnessCalculator calculator = new GoodnessCalculator(5, matcher, entropyCalculator);
        // with the middle 3 being green, only the two outside ones should be tried. There are 8 combinations, with 1
        // invalid one.
        calculator.calculateGoodness("abcde",
                new Color[]{Color.GRAY, Color.GREEN, Color.GREEN, Color.GREEN, Color.YELLOW},
                NUM_CHOICES);
        verify(entropyCalculator, times(7)).calculateEntropy(NUM_MATCHING, NUM_CHOICES);
    }

}
