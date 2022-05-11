package wordle;

import java.io.IOException;

public class Solver {

    private final int wordLength;
    private final WordMatcher matcher;
    private final Dictionary dictionary;
    private final GoodnessCalculator goodnessCalculator;

    public Solver(int wordLength, DictionaryFileLoader dictionaryLoader) {
        this.wordLength = wordLength;

        try {
            dictionary = dictionaryLoader.buildDictionary();
        } catch (IOException e) {
            throw new IllegalArgumentException("The dictionary could not be indexed", e);
        }

        // TODO: inject these for testability
        matcher = new WordMatcher(dictionary);
        goodnessCalculator = new GoodnessCalculator(wordLength, matcher);

        System.out.println("Dictionary has " + dictionary.size() + " " + wordLength + "-letter words");
    }

    /**
     * Calculate an optimal first word to play.
     */
    public String findFirstWord() {
        return findNextWord(new Color[wordLength]);
    }

    /**
     * Calculates an optimal next word to play. Before picking a word, it filters the dictionary to match the outcome
     * of the previous round. This will actually modify the dictionary.
     *
     * @param outcome the colors that the game provided.
     */
    public String findNextWord(String previousGuess, Color[] outcome) {
        dictionary.intersect(matcher.getMatchingWords(previousGuess.toCharArray(), outcome));

        System.out.println("There are " + dictionary.size() + " word(s) remaining");
        if (dictionary.size() <= 10) {
            System.out.println("    " + dictionary.getWords());
        }

        return findNextWord(outcome);
    }

    private String findNextWord(Color[] knownColors) {
        double bestAverage = 0.0;
        String bestWord = null;

        if (dictionary.size() == 1) {
            return dictionary.getWords().get(0);
        }

        for (String word : dictionary.getWords()) {

            double average = goodnessCalculator.calculateGoodness(word, knownColors, dictionary.size());
            if (average > bestAverage) {
                bestAverage = average;
                bestWord = word;
            }
        }
        return bestWord;
    }
}
