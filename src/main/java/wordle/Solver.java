package wordle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Solver {

    private final int wordLength;
    private final int numColorings;
    private final WordMatcher matcher;
    private Dictionary dictionary;

    public Solver(int wordLength, DictionaryFileLoader dictionaryLoader) {
        this.wordLength = wordLength;
        numColorings = (int) Math.pow(3, wordLength);

        try {
            dictionary = dictionaryLoader.buildDictionary();
        } catch (IOException e) {
            throw new IllegalArgumentException("The dictionary could not be indexed", e);
        }

        matcher = new WordMatcher(dictionary);

        System.out.println("Dictionary has " + dictionary.size() + " " + wordLength + "-letter words");
    }

    /**
     * Calculate an optimal next word to play.
     */
    public String findNextWord() {
        int tryNum = 0;

        double bestAverage = 0.0;
        String bestWord = null;
        for (String word : dictionary.getWords()) {
            tryNum++;
            if (tryNum % 20 == 0) {
                System.out.println("trying word #" + tryNum);
            }

            double average = findAverageEntropy(word);
            if (average > bestAverage) {
                bestAverage = average;
                bestWord = word;
                System.out.println("best so far: " + word);
            }
        }
        return bestWord;
    }

    /**
     * Filters the dictionary to match a given outcome from the game. After the game provides the colors as feedback,
     * the only possible words are the ones given.
     *
     * @param outcome the colors that the game provided.
     */
    public String findNextWord(Color[] outcome) {
        dictionary = new Dictionary(matcher.getMatchingWords(outcome));
        return findNextWord();
    }

    /**
     * If we guess the given word, the remaining choices will be reduced by some amount. Since we don't know the target
     * word, we don't know that amount. Assuming the target word is selected at random from the dictionary, then we can
     * calculate the average reduction, assuming that all colorings are equally likely.
     *
     * @param guess the given guess
     * @return the expected reduction of the remaining choices, as a percentage of numChoices
     */
    double findAverageEntropy(String guess) {
        // enumerate all possible coloring outcomes, and for each one, find out how many words match it.
        List<Double> entropies = new ArrayList<>(numColorings);
        analyzePossibleOutcomes(guess.toCharArray(), 0, new Color[wordLength], dictionary.size(), entropies);

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
                probabilities.add(-1 * (pMatch * log2(pMatch) + pNoMatch * log2(pNoMatch)));
            } else {
                probabilities.add(0.0);
            }
            return;
        }

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

    public static void main(String[] args) {
        Solver s = new Solver(5, new DictionaryFileLoader("/dictionary.txt"));

        System.out.println("crane " + s.findAverageEntropy("crane"));
        System.out.println("soare " + s.findAverageEntropy("soare"));
        System.out.println("pores " + s.findAverageEntropy("pizza"));
        System.out.println("pales " + s.findAverageEntropy("pizza"));
        System.out.println("pizza " + s.findAverageEntropy("pizza"));
        System.out.println("squaw " + s.findAverageEntropy("squaw"));
        System.out.println("buxom " + s.findAverageEntropy("buxom"));
        System.out.println("mamma " + s.findAverageEntropy("mamma"));
        System.out.println("stump " + s.findAverageEntropy("stump"));
        System.out.println("fuzzy " + s.findAverageEntropy("fuzzy"));

        //System.out.println(s.findFirstWord());
    }

}
