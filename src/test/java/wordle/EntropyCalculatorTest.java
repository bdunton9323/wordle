package wordle;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

public class EntropyCalculatorTest {

    private EntropyCalculator calculator = new EntropyCalculator();

    @Test
    public void testCertainProbabilities() {
        assertThat(calculator.calculateEntropy(5, 5)).isEqualTo(0.0, offset(0.001));
    }

    @Test
    public void testBigSmallSplit() {
        assertThat(calculator.calculateEntropy(1, 10)).isEqualTo(0.4690, offset(0.000005));
    }

    @Test
    public void testSmallBigSplit() {
        assertThat(calculator.calculateEntropy(9, 10)).isEqualTo(0.4690, offset(0.000005));
    }

    @Test
    public void testZeroMatching() {
        assertThat(calculator.calculateEntropy(0, 10)).isEqualTo(0.0, offset(0.000005));
    }

    @Test
    public void testZeroMatchingOutOfZero() {
        assertThat(calculator.calculateEntropy(0, 0)).isEqualTo(0.0, offset(0.000005));
    }

    @Test
    public void testNaN() {
        // there can't be 10 possibilities from 3 words
        assertThat(calculator.calculateEntropy(10, 3)).isEqualTo(0.0);
    }
}
