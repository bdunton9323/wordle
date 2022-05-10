package wordle;

import java.util.HashSet;
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

        // I could speed this up by ignoring invalid cases
        // e.g. the outcome GGGGY can never happen. The matching word set will always be empty.
        // likewise, 'pizza' should never match 'unzip' over YYGY-

        // aba bab should be YY-, not YYY

        Set<String> possible = new HashSet<>(index.getDictionary());

        // optimize for double letters
        Set<Character> seen = new HashSet<>();

        for (int i = 0; i < letters.length; i++) {
            if (outcome[i] == Color.GRAY) {
                if (seen.contains(letters[i])) {
                    continue;
                }
                removeWordsWithLetter(letters[i], possible);
            } else if (outcome[i] == Color.YELLOW) {
                if (seen.contains(letters[i])) {
                    continue;
                }
                // remove everything that has the letter in this spot
                removeWordsWithExactLetter(letters[i], i, possible);
                // remove everything that doesn't have this letter
                removeWordsWithoutLetter(letters[i], possible);
            } else if (outcome[i] == Color.GREEN) {
                keepWordsWithExactLetter(letters[i], i, possible);
            }

            seen.add(letters[i]);
        }

        return possible.size();
    }

    private void removeWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) == letter);
    }

    private void keepWordsWithExactLetter(char letter, int position, Set<String> words) {
        words.removeIf(word -> word.charAt(position) != letter);
    }

    private void removeWordsWithoutLetter(char letter, Set<String> words) {
        words.removeIf(word -> !word.contains(Character.toString(letter)));
    }

    private void removeWordsWithLetter(char letter, Set<String> words) {
        words.removeIf(word -> word.contains(Character.toString(letter)));
    }
}
