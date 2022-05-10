package wordle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class WordMatcherTest {
    @Mock
    private WordIndex wordIndex;

    private WordMatcher matcher;

    @Before
    public void setUp() {
        openMocks(this);
        matcher = new WordMatcher(wordIndex);
    }

    @Test
    public void testAllGreen() {
        withDictionary("cat", "bar", "bat");
        int count = matcher.countMatchingWords("cat".toCharArray(),
                new Color[]{Color.GREEN, Color.GREEN, Color.GREEN});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSingleYellowNoReduction() {
        withDictionary("cat", "bar", "bat");
        int count = matcher.countMatchingWords("a".toCharArray(), new Color[]{Color.YELLOW});
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void testSingleYellowWithReduction() {
        withDictionary("cat", "bar", "bat");
        int count = matcher.countMatchingWords("r".toCharArray(), new Color[]{Color.YELLOW});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testMultipleYellow() {
        withDictionary("cat", "bar", "bat");
        int count = matcher.countMatchingWords("tc".toCharArray(), new Color[]{Color.YELLOW, Color.YELLOW});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testDoubleLetterYellow() {
        withDictionary("cool", "book", "boot");
        int count = matcher.countMatchingWords("tool".toCharArray(),
                new Color[]{Color.GRAY, Color.YELLOW, Color.YELLOW, Color.GRAY});
        assertThat(count).isEqualTo(0);

        count = matcher.countMatchingWords("oxxo".toCharArray(),
                new Color[]{Color.YELLOW, Color.GRAY, Color.GRAY, Color.YELLOW});
        assertThat(count).isEqualTo(3);
    }

    @Test
    public void testSingleGray() {
        withDictionary("cat", "bar", "bat");
        int count = matcher.countMatchingWords("b".toCharArray(), new Color[]{Color.GRAY});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testMultipleGray() {
        withDictionary("ab", "ac", "cd", "dx");
        int count = matcher.countMatchingWords("bc".toCharArray(), new Color[]{Color.GRAY, Color.GRAY});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testDoubleLetterGray() {
        withDictionary("book", "soot", "sand");
        int count = matcher.countMatchingWords("oo".toCharArray(), new Color[]{Color.GRAY, Color.GRAY});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testSingleGreen() {
        withDictionary("a");
        int count = matcher.countMatchingWords("a".toCharArray(), new Color[]{Color.GREEN});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testMultipleGreen() {
        withDictionary("ca", "cx", "bz");
        int count = matcher.countMatchingWords("cx".toCharArray(), new Color[]{Color.GREEN, Color.GREEN});
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testGreenNoMatches() {
        withDictionary("abc", "def");
        int count = matcher.countMatchingWords("ax".toCharArray(), new Color[]{Color.GREEN, Color.GREEN});
        assertThat(count).isEqualTo(0);
    }

    @Test
    public void testMultipleColors() {
        withDictionary("south", "antic", "based", "stood");
        int count = matcher.countMatchingWords("acids".toCharArray(),
                new Color[]{Color.GREEN, Color.YELLOW, Color.YELLOW, Color.GRAY, Color.GRAY});
        // just antic
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testDoubleLetterInTarget() {
        withDictionary("aba", "bab");
        // the only way both a's can be yellow is if the target word has two A's. Not only are there no words with two
        // A's that aren't all green, but in a 3-letter word it's not possible for both A's to be wrong.
        int count = matcher.countMatchingWords("aba".toCharArray(),
                new Color[]{Color.YELLOW, Color.YELLOW, Color.YELLOW});
        assertThat(count).isEqualTo(0);

        count = matcher.countMatchingWords("aba".toCharArray(),
                new Color[]{Color.YELLOW, Color.YELLOW, Color.GRAY});
        // should match "bab"
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testDoubleLetterInGuess() {
        withDictionary("abc", "xyz");

        // either of the C's could be yellow and the match would still be the same, so
        // define the canonical representation to be the one where the leftmost occurrence is
        // colored.
        int count = matcher.countMatchingWords("cca".toCharArray(),
                new Color[]{Color.YELLOW, Color.GRAY, Color.YELLOW});
        assertThat(count).isEqualTo(1);

        // valid color combination, but not the canonical form, so should not match
        count = matcher.countMatchingWords("cca".toCharArray(),
                new Color[]{Color.GRAY, Color.YELLOW, Color.YELLOW});
        assertThat(count).isEqualTo(0);

        count = matcher.countMatchingWords("bba".toCharArray(),
                new Color[]{Color.GRAY, Color.GREEN, Color.YELLOW});
        assertThat(count).isEqualTo(1);

        // this pattern would indicate the target should have two B's, so it shouldn't be a match
        count = matcher.countMatchingWords("abb".toCharArray(),
                new Color[]{Color.GRAY, Color.GREEN, Color.YELLOW});
        assertThat(count).isEqualTo(0);
    }

    private void withDictionary(String... words) {
        when(wordIndex.getDictionary()).thenReturn(listOf(words));
    }

    private List<String> listOf(String... items) {
        return new ArrayList<>(Arrays.asList(items));
    }
}
