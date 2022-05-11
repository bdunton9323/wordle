package wordle;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates how good a given guess is expected to be, for an average target word in the dictionary.
 */
public class GoodnessCalculator {

    private final int wordLength;
    private final int numColorings;
    private final WordMatcher matcher;

    public GoodnessCalculator(int wordLength, WordMatcher matcher) {
        this.wordLength = wordLength;
        numColorings = (int) Math.pow(3, wordLength);
        this.matcher = matcher;
    }

    /**
     * If we guess the given word, the remaining choices will be reduced by some amount. Since we don't know the target
     * word, we don't know that amount. Assuming the target word is selected at random from the dictionary, then we can
     * calculate the average reduction, assuming that all colorings are equally likely.
     *
     * @param guess the given guess
     * @param knownColors the colors that are known already. Only the green ones matter.
     * @return the expected reduction of the remaining choices, as a percentage of numChoices
     */
    public double calculateGoodness(String guess, Color[] knownColors, int dictionarySize) {
        // This could be improved. It doesn't need to be this big except in the worst case.
        List<Double> entropies = new ArrayList<>(numColorings);

        Color[] stateSpace = new Color[wordLength];
        System.arraycopy(knownColors, 0, stateSpace, 0, wordLength);

        analyzePossibleOutcomes(guess.toCharArray(), 0, stateSpace, dictionarySize, entropies);

        // the average entropy of the target word
        return average(entropies);
    }

    /**
     * Goes through each of the possible colorings that can arise, and analyzes the probability that a randomly chosen
     * target word is in the set of words revealed by that pattern.
     * <p/>
     * For each letter, the possibilities are (gray, green, yellow), which leaves 3^wordLength possible color outcomes.
     * Each of those outcomes has some probability of matching the target word. The amount of information gained is the
     * entropy of the outcomes: 1) the target is matched by the pattern, 2) the target is not matched by the pattern.
     * <p/>
     * There may be a more intelligent way to do this, but it seems to work.
     *
     * @param letters The letters in the guess
     * @param index the index whose color we are choosing
     * @param outcome the outcome being tested
     * @param numChoices the number of words being chosen against
     * @param probabilities the probability that the target word is matched by this outcome coloring. One probability
     * per valid color outcome tested.
     */
    private void analyzePossibleOutcomes(char[] letters, int index, Color[] outcome, int numChoices,
            List<Double> probabilities) {

        // TODO: the logic of enumerating the color choices can be done by a different class. That way it can be
        //       tested in isolation of the entropy calculation. Could even define the color enumerator as an Iterator<Color[]>.
        if (index == wordLength) {
            // some patterns are invalid
            if (!isOutcomeValid(outcome)) {
                return;
            }

            int numMatching = matcher.countMatchingWords(letters, outcome);
            double pMatch = numMatching / (double) numChoices;
            double pNoMatch = (numChoices - numMatching) / (double) numChoices;

            // calculate the Shannon entropy of the possible outcomes. The outcomes here are:
            //     - chance that the target word resulted in this color outcome,
            //     - chance that it didn't
            if (numMatching != 0) {
                double entropy = -1 * (pMatch * log2(pMatch) + pNoMatch * log2(pNoMatch));
                if (!Double.isNaN(entropy)) {
                    probabilities.add(entropy);
                } else {
                    probabilities.add(0.0);
                }
            } else {
                probabilities.add(0.0);
            }
            return;
        }

        if (outcome[index] == Color.GREEN) {
            // if we know this letter already, don't try anything else in that spot
            analyzePossibleOutcomes(letters, index + 1, outcome, numChoices, probabilities);
        } else {

            // use backtracking to try each of the possible colorings that can result

            // contains this letter
            outcome[index] = Color.YELLOW;
            analyzePossibleOutcomes(letters, index + 1, outcome, numChoices, probabilities);

            // contains this letter in this position
            outcome[index] = Color.GREEN;
            analyzePossibleOutcomes(letters, index + 1, outcome, numChoices, probabilities);

            // does not contain this letter
            outcome[index] = Color.GRAY;
            analyzePossibleOutcomes(letters, index + 1, outcome, numChoices, probabilities);

            outcome[index] = null;
        }
    }

    private boolean isOutcomeValid(Color[] outcome) {
        int numGreen = 0;//= Arrays.stream(outcome).map(c -> c == Color.GREEN ? 1 : 0).reduce(0, Integer::sum);
        int numYellow = 0;
        for (Color c : outcome) {
            if (c == Color.GREEN) {
                numGreen++;
            } else if (c == Color.YELLOW) {
                numYellow++;
            }
        }
        if (numGreen == wordLength - 1 && numYellow > 0) {
            return false;
        }
        return true;
    }

    private double average(List<Double> nums) {
        double sum = nums.stream().reduce(0.0, Double::sum);
        return sum / nums.size();
    }

    private double log2(double n) {
        return Math.log(n) / Math.log(2);
    }
}
