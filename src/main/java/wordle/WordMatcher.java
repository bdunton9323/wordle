package wordle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WordMatcher {

    private final WordIndex index;

    public WordMatcher(WordIndex index) {
        this.index = index;
    }

    public int countMatchingWords(char[] letters, Color[] outcome) {
        if (letters.length != outcome.length) {
            throw new IllegalArgumentException("Word length must be the same size as the color pattern");
        }

        // should probably do this in the pattern generator code. some cases can never happen:
        // e.g. GGGGY, GYGGG, ...
//        if (isInvalid(outcome)) {
//            return 0;
//        }


        // apply the colors one by one
        // tally the occurrences of each letter as it is processed -> occur(letter)
        // if I see a gray, eliminate only the words with more than occur(letter) occurrences
        //      freeze the count so that a future yellow doesn't increase it
        // if I see a yellow, add one to the occurrences, and filter the set to the words with that many
        // if I see a green, increase the count, even if it's frozen.
        //
        // if guess is CCA and pattern is Y-Y, and dictionary has ABC:
        // 0: Yellow, so occur(C) = 1. Retain words with 1 or more C's.
        // 1: Gray, so freeze occur(C) at 1. Retain words with exactly occur(C) C's.
        // 2: Yellow, so occur(A) = 1. Retain all words with 1 or more A's.

        // can I detect this case up front? Can I do the tallying up front and make the filtering easier?
        // if guess is CCAA and pattern is YY-Y, and dictionary has XXCC:
        // 0: Yellow, so occur(C) = 1. Retain words with 1 or more C's.
        // 1: Yellow, so occur(C) = 2. Retain words with 2 or more C's.
        // 2: Gray, so freeze occur(A) at 0. Remove all words with any A's.
        // 3: Yellow, but occur(A) is frozen at 0. There are no more words in the set with an A, return empty.

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
                    return 0;
                } else {
                    occur[letterIdx]++;
                }
            } else if (c == Color.GREEN) {
                occur[letterIdx]++;
            }
        }

        Set<String> possible = new HashSet<>(index.getDictionary());

        for (int i = 0; i < occur.length; i++) {
            char letter = (char) (i + 'a');

            if (occur[i] == 0) {
                // TODO: this needs to distinguish between "this letter really does not appear"
                //       vs. "this letter was not in the target so we don't know if it appears"
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

        return possible.size();
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
