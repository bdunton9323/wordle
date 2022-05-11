package wordle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WordMatcher {

    private final Dictionary dictionary;

    public WordMatcher(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public int countMatchingWords(char[] letters, Color[] outcome) {
        return getMatchingWords(letters, outcome).size();
    }

    public Set<String> getMatchingWords(char[] letters, Color[] outcome) {
        if (letters.length != outcome.length) {
            throw new IllegalArgumentException("Word length must be the same size as the color pattern");
        }

        // apply the colors one by one
        // tally the occurrences of each letter as it is processed -> occur(letter)
        // if I see a gray, eliminate only the words with more than occur(letter) occurrences
        //      freeze the count so that a future yellow doesn't increase it
        // if I see a yellow, add one to the occurrences, and filter the set to the words with that many
        // if I see a green, increase the count, even if it's frozen.

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
                retainWordsWithExactlyNOccurrences(letter, occur[i], possible);
            } else {
                retainWordsWithAtLeastNOccurrences(letter, occur[i], possible);
            }
        }

        // filter for positions
        for (int i = 0; i < letters.length; i++) {
            if (outcome[i] == Color.GRAY || outcome[i] == Color.YELLOW) {
                removeWordsWithExactLetter(letters[i], i, possible);
            } else if (outcome[i] == Color.GREEN) {
                retainWordsWithExactLetter(letters[i], i, possible);
            }
        }

        return possible;
    }


    private void removeWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) == letter);
    }

    private void retainWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) != letter);
    }

    private void retainWordsWithExactlyNOccurrences(char letter, int occurrences, Set<String> words) {
        Iterator<String> it = words.iterator();
        while (it.hasNext()) {
            char[] chars = it.next().toCharArray();
            int count = 0;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == letter) {
                    count++;
                }
            }
            if (count != occurrences) {
                it.remove();
            }
        }
    }

    // TODO: remove duplication between this and the above
    private void retainWordsWithAtLeastNOccurrences(char letter, int occurrences,
            Set<String> words) {
        Iterator<String> it = words.iterator();
        while (it.hasNext()) {
            char[] chars = it.next().toCharArray();
            int count = 0;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == letter) {
                    count++;
                }
            }
            if (count < occurrences) {
                it.remove();
            }
        }
    }

    private void removeWordsWithoutLetter(char letter, Set<String> words) {
        words.removeIf(word -> !word.contains(Character.toString(letter)));
    }

    private void removeWordsWithLetter(char letter, Set<String> words) {
        words.removeIf(word -> word.contains(Character.toString(letter)));
    }
}
