package wordle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            // TODO: until I get double letters working, skip them
            if (hasRepeatedLetter(word)) {
                continue;
            }

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

    private boolean hasRepeatedLetter(String word) {
        Set<Character> s = new HashSet<>();
        for (char c : word.toCharArray()) {
            if (!s.add(c)) {
                return true;
            }
        }
        return false;
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

        // TODO: another way to possibly think about this:
        // pretend each word is the target. Calculate the color pattern. Count how many total unique patterns there are.
        // what does it mean for a guess to give me information about the target word?
        //
        // if the target word is w, the pattern will be c. c also matches n other words, so all you know is that w is in that set.


        // TODO: do I need to do a weighted average? The single-yellow case is dominating the average, even though it
        //       gives very little info (at least intuitively - maybe mathematically it works out?). At the very least,
        //       it feels like it's overcounting the target words because one target could fit more than one pattern
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
            // numChoices - countMatchingWords() is the number of words filtered out
            // a good guess is one that filters out a lot of words on average for any possible target picked at random
            //probabilities.add((numChoices - matcher.countMatchingWords(letters, outcome)) / (double) numChoices);

            // TODO: This is why it isn't working well: the number of words eliminated is not the proper metric
            //       I might really need entropy. If the word fuzzy has no matching words, then it will add the biggest
            //       possible number to this list. If it eliminates most of the possibilities, then the chance the target
            //       word is in that small set is very low. The best guess might be the one that gives the target a 50%
            //       chance of being right (as close to 50% as possible).
            // define p(x) as the probability that this color pattern matches the target word
            //             = the probability that the target word is in the set of matching words
            //             = matching / total
            // picking a color pattern means: the target word is in the set of words that match this pattern
            // the probability that any random target word matches the pattern is the probability that the target word
            // is in the matching set, which is matching / total
            //
            // so that means I use p(x) = matching/total in calculating the entropy
            //probabilities.add(((double) numChoices - matcher.countMatchingWords(letters, outcome)) / 100);
            int numMatching = matcher.countMatchingWords(letters, outcome);
            double pMatch = numMatching / (double) numChoices;
            double pNoMatch = (numChoices - numMatching) / (double) numChoices;

            if (numMatching == 0) {
                //probabilities.add(0.0);
            } else {
                probabilities.add(-1 * (pMatch * log2(pMatch) + pNoMatch * log2(pNoMatch)));
            }
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
//        int count = 0;
//        for (double n : nums) {
//            if (n != 45.67) count++;
//        }
//        System.out.println("number of cases that resulted in some information: " + count);

        double sum = nums.stream().reduce(0.0, Double::sum);
        return sum / nums.size();
    }

    private double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    public static void main(String[] args) throws IOException {
        Solver s = new Solver(5, new IndexBuilder());

        // Here is one problem:
        // pizza is matching unzip for YYGY- and YYG--, when it should only match the latter (there aren't two z's in unzip)
        // how do I make my matcher distinguish those?

//        System.out.println("pizza " + s.findAverageReduction("pizza", s.getDictionarySize()));
//        System.out.println("squaw " + s.findAverageReduction("squaw", s.getDictionarySize()));
//        System.out.println("crane " + s.findAverageReduction("crane", s.getDictionarySize()));
//        System.out.println("buxom " + s.findAverageReduction("buxom", s.getDictionarySize()));
//        System.out.println("soare " + s.findAverageReduction("soare", s.getDictionarySize()));
//        System.out.println("tares " + s.findAverageReduction("tares", s.getDictionarySize()));
//        System.out.println("mamma " + s.findAverageReduction("mamma", s.getDictionarySize()));
//        System.out.println("stump " + s.findAverageReduction("stump", s.getDictionarySize()));
//        System.out.println("fuzzy " + s.findAverageReduction("fuzzy", s.getDictionarySize()));

        //System.out.println(s.findFirstWord());
    }

}
