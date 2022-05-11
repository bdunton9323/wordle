package wordle;

public class EntropyCalculator {

    /**
     * Calculates the Shannon entropy of a guess paired with its outcome.
     *
     * @param numMatchingWords the number of words that match the guess through one particular color combination.
     * @param numChoices the number of choices this guess was chosen out of
     * @return the entropy of the guess
     */
    public double calculateEntropy(int numMatchingWords, int numChoices) {

        // probability that the target word is in the dictionary partition defined by this outcome
        double pMatch = numMatchingWords / (double) numChoices;

        // probability that the target word is not in this outcome's partition
        double pNoMatch = (numChoices - numMatchingWords) / (double) numChoices;

        // calculate the Shannon entropy of the possible outcomes. The outcomes here are:
        //     - the target word resulted in this color outcome,
        //     - chance that it didn't
        if (numMatchingWords != 0) {
            double entropy = -1 * (pMatch * log2(pMatch) + pNoMatch * log2(pNoMatch));
            if (!Double.isNaN(entropy)) {
                return entropy;
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    private double log2(double n) {
        return Math.log(n) / Math.log(2);
    }
}
