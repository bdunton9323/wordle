package wordle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashSet;
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
        int count = matcher.countMatchingWords("oo".toCharArray(), new Color[]{Color.YELLOW, Color.YELLOW});
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

    private void withDictionary(String... words) {
        when(wordIndex.getDictionaryCopy()).thenReturn(setOf(words));
    }

    private Set<String> setOf(String... items) {
        return new HashSet<>(Arrays.asList(items));
    }
}
