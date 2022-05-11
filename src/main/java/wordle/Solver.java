package wordle;

public class Solver {

    private final int wordLength;
    private final Dictionary dictionary;
    private final WordMatcher matcher;
    private final GoodnessCalculator goodnessCalculator;

    public Solver(int wordLength, Dictionary dictionary, WordMatcher wordMatcher,
            GoodnessCalculator goodnessCalculator) {
        this.wordLength = wordLength;
        this.dictionary = dictionary;
        this.matcher = wordMatcher;
        this.goodnessCalculator = goodnessCalculator;
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

        System.out.println("Getting close! Only " + dictionary.size() + " possible word(s) remaining:");
        if (dictionary.size() <= 10) {
            System.out.println("    " + dictionary.getWords());
        }

        return findNextWord(outcome);
    }

    private String findNextWord(Color[] knownColors) {
        if (dictionary.size() == 1) {
            return dictionary.getWords().get(0);
        }

        double bestAverage = 0.0;
        String bestWord = null;

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
