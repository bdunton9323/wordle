package wordle;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DictionaryTest {

    @Test
    public void testIntersect() {
        Dictionary d = new Dictionary(List.of("heaps", "purge", "alarm"));
        d.intersect(Set.of("heaps", "alarm"));
        assertThat(d.getWords()).containsExactlyInAnyOrder("heaps", "alarm");
    }

    @Test
    public void testAddWord() {
        Dictionary d = new Dictionary();
        d.addWord("heaps");
        d.addWord("fizzy");
        assertThat(d.getWords()).containsExactlyInAnyOrder("heaps", "fizzy");
    }
}
