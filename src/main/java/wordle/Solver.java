package wordle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Solver {
    private static final String DICTIONARY_PATH = "/dictionary.txt";
    private static final char[] ALL_LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final int wordLength;
    private final int numColorings;

    private final WordIndex index;
    private final WordMatcher matcher;

    public Solver(int wordLength, IndexBuilder indexBuilder) {
        this.wordLength = wordLength;
        numColorings = (int) Math.pow(3, wordLength);

        try {
            index = indexBuilder.buildIndex(wordLength);
        } catch (IOException e) {
            throw new IllegalArgumentException("The dictionary could not be indexed", e);
        }

        matcher = new WordMatcher(index);

        System.out.println("Dictionary has " + index.getDictionarySize() + " " + wordLength + "-letter words");
    }

    public int getDictionarySize() {
        return index.getDictionarySize();
    }

//    public void calculateEntropyOfStartingLetters() {
//        Map<Character, Set<String>> startingLetters = letterByPosition.get(0);
//        for (Character c : startingLetters.keySet()) {
//            int startsWith = startingLetters.get(c).size();
//            int startsWithout = dictionarySize - startsWith;
//            double probabilityWith = (double) startsWith / dictionarySize;
//            double probabilityWithout = (double) startsWithout / dictionarySize;
//            double entropyStartsWith = -(probabilityWith * log2(probabilityWith) + probabilityWithout * log2(probabilityWithout));
//            double entropyStartsWithout = -(probabilityWith * log2(probabilityWith) + probabilityWithout * log2(probabilityWithout));
//            //double gain = ((double) 1 / dictionarySize) - (probabilityWith * entropyStartsWith + probabilityWithout * entropyStartsWithout);
//
//            System.out.println("Starting with " + c + ", entropy: " + entropyStartsWith);
//
//        }
//    }

    public String findFirstWord() {
        // for each word, go through the possible outcomes and calculate the number of words in each
        // the word that results in the most number of small sets is the winner
        // each outcome would be a "feature". Can calculate the entropy and information gain by looking at the sizes
        // of each state's word set, and the probability that a word from the dictionary is in that set.

        int tryNum = 0;

        double bestAverage = 0.0;
        String bestWord = null;
        for (String word : index.getDictionary()) {
            tryNum++;
            if (tryNum % 10 == 0) {
                System.out.println("trying word #" + tryNum);
            }

            double average = findAverageReduction(word, index.getDictionarySize());
            if (average > bestAverage) {
                bestAverage = average;
                bestWord = word;
                System.out.println("best so far: " + word);
            }
        }
        return bestWord;
    }

    /**
     * If we guess the given word, the remaining choices will be reduced by some amount. Since we don't know the target
     * word, we don't know that amount. Assuming the target word is selected at random from the dictionary, then we can
     * calculate the average reduction, assuming that all colorings are equally likely.
     *
     * @param guess the given guess
     * @param numChoices the number of choices out of which this guess was selected
     * @return the expected reduction of the remaining choices, as a percentage of numChoices
     */
    double findAverageReduction(String guess, int numChoices) {
        // enumerate all possible coloring outcomes, and for each one, find out how many words match it. For each
        // letter, the possibilities are (gray, green, yellow), which leaves 3^wordLength possibilities
        List<Double> probabilities = new ArrayList<>(numColorings);
        enumerate(guess.toCharArray(), 0, new Color[wordLength], numChoices, probabilities);
        return average(probabilities);
    }

    /**
     *
     * @param letters
     * @param index
     * @param outcome
     * @param numChoices
     * @param probabilities the probability of the target word being matched by this outcome coloring
     */
    private void enumerate(char[] letters, int index, Color[] outcome, int numChoices, List<Double> probabilities) {
        if (index == wordLength) {
            // TODO: do I need 1/probability?
            probabilities.add((double) matcher.countMatchingWords(letters, outcome) / numChoices);
            return;
        }

        // use backtracking to try each of the possible colorings

        // TODO: I can save work by backtracking the items in the possibilities set too. Don't recompute everything
        //       when going from YYYYY to YYYYG
        // e.g. if the guess is BATHE and it comes up YY---:
        //       when we find all the words with B in it, we only need to search that subset for the ones with BA

        // contains this letter
        outcome[index] = Color.YELLOW;
        enumerate(letters, index + 1, outcome, numChoices, probabilities);

        // contains this letter in this position
        outcome[index] = Color.GREEN;
        enumerate(letters, index + 1, outcome, numChoices, probabilities);

        // does not contain this letter
        outcome[index] = Color.GRAY;
        enumerate(letters, index + 1, outcome, numChoices, probabilities);
    }

    private double average(List<Double> nums) {
        double sum = nums.stream().reduce(0.0, Double::sum);
        return sum / nums.size();
    }

    private double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    public static void main(String[] args) throws IOException {
        Solver s = new Solver(5, new IndexBuilder());

        System.out.println(s.findAverageReduction("mamma", s.getDictionarySize()));
        System.out.println(s.findAverageReduction("soare", s.getDictionarySize()));
        System.out.println(s.findAverageReduction("tares", s.getDictionarySize()));
        System.out.println(s.findAverageReduction("stump", s.getDictionarySize()));
        System.out.println(s.findAverageReduction("pizza", s.getDictionarySize()));
        System.out.println(s.findAverageReduction("fuzzy", s.getDictionarySize()));

        //System.out.println(s.findFirstWord());
    }

}
