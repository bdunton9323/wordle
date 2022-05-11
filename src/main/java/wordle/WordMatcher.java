package wordle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class WordMatcher {

    private final Dictionary dictionary;

    public WordMatcher(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public int countMatchingWords(char[] letters, Color[] outcome) {
        return getMatchingWords(letters, outcome).size();
    }

    /**
     * Gets the list of words in the Dictionary that match a given color scheme when applied to a guess.
     * There can be more than one way to express the colors for a guess. This expects the colors as Wordle would
     * output them, in terms of handling double letters.
     *
     * TODO: am I sure this is how Wordle outputs the colors? It wouldn't be hard to support a more generalized scheme
     *       that could accept any color order. Somewhere there needs to be a notion of the canonical scheme in order
     *       to avoid filtering the dictionary for many equivalent colorings.
     *
     * @param letters the letters of the guessed word
     * @param outcome the colors to apply to the guess
     * @return the list of words matching the given coloring
     */
    public Set<String> getMatchingWords(char[] letters, Color[] outcome) {
        if (letters.length != outcome.length) {
            throw new IllegalArgumentException("Word length must be the same size as the color pattern");
        }

        // tally the occurrences of each letter as it is processed
        // if we see a gray:
        //      freeze the count so that a future yellow doesn't increase it
        //      eliminate only the words with more than occur[letter] occurrences
        // if we see a yellow, add one to the occurrences, and filter the set to the words with that many
        // if we see a green, increase the count, even if it's frozen.

        int[] occur = new int[26];
        boolean[] frozen = new boolean[26];
        for (int i = 0; i < letters.length; i++) {
            Color c = outcome[i];
            int letterIdx = letters[i] - 'a';

            if (c == Color.GRAY) {
                frozen[letterIdx] = true;
            } else if (c == Color.YELLOW) {
                // if we see a letter yellow after seeing it gray, then this is an invalid
                // coloring. We will see the canonical form of this coloring eventually.
                if (frozen[letterIdx]) {
                    return Set.of();
                } else {
                    occur[letterIdx]++;
                }
            } else if (c == Color.GREEN) {
                occur[letterIdx]++;
            }
        }

        Set<String> possible = new HashSet<>(dictionary.getWords());

        for (int i = 0; i < occur.length; i++) {
            char letter = (char) (i + 'a');

            if (occur[i] == 0) {
                // if we know a letter doesn't appear (vs. not having any info about it), we will have seen a gray
                if (frozen[i]) {
                    removeWordsWithLetter(letter, possible);
                }
            } else if (frozen[i]) {
                // The letter is in the word. Worry about where it is later.
                //removeWordsWithoutLetter(letter, possible);
                keepWordsWithExactlyNOccurrences(letter, occur[i], possible);
            } else {
                keepWordsWithAtLeastNOccurrences(letter, occur[i], possible);
            }
        }

        // filter for positions
        for (int i = 0; i < letters.length; i++) {
            if (outcome[i] == Color.GRAY || outcome[i] == Color.YELLOW) {
                removeWordsWithExactLetter(letters[i], i, possible);
            } else if (outcome[i] == Color.GREEN) {
                keepWordsWithExactLetter(letters[i], i, possible);
            }
        }

        return possible;
    }


    private void removeWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) == letter);
    }

    private void keepWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) != letter);
    }

    private void keepWordsWithExactlyNOccurrences(char letter, int occurrences, Set<String> words) {
        removeLetterCountMatchingPredicate(letter, words, actualCount -> actualCount != occurrences);
    }

    private void keepWordsWithAtLeastNOccurrences(char letter, int occurrences,
            Set<String> words) {
        removeLetterCountMatchingPredicate(letter, words, actualCount -> actualCount < occurrences);
    }

    private void removeLetterCountMatchingPredicate(char letter, Set<String> words,
            Predicate<Integer> removeIf) {
        Iterator<String> it = words.iterator();
        while (it.hasNext()) {
            char[] chars = it.next().toCharArray();
            int count = 0;
            for (char c : chars) {
                if (c == letter) {
                    count++;
                }
            }
            if (removeIf.test(count)) {
                it.remove();
            }
        }
    }

    private void removeWordsWithLetter(char letter, Set<String> words) {
        words.removeIf(word -> word.contains(Character.toString(letter)));
    }
}
