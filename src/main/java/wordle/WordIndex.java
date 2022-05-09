package wordle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WordIndex {
    private static final char[] ALL_LETTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    private final List<String> dictionary = new ArrayList<>(5000);
    private final Map<Character, Set<String>> hasLetter = new HashMap<>();
    private final Map<Character, Set<String>> doesNotHaveLetter = new HashMap<>();
    private final Map<Integer, Map<Character, Set<String>>> letterByPosition = new HashMap<>();

    public WordIndex(int wordLength) {
        for (int i = 0; i < wordLength; i++) {
            letterByPosition.put(i, new HashMap<>());
        }
    }

    public void indexWord(String word) {
        char[] letters = word.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            hasLetter.computeIfAbsent(letters[i], c -> new HashSet<>()).add(word);
            letterByPosition.get(i).computeIfAbsent(letters[i], HashSet::new).add(word);
        }

        for (char letterInAlpha : ALL_LETTERS) {
            for (char letterInWord : letters) {
                if (letterInAlpha == letterInWord) {
                    break;
                }
            }
            doesNotHaveLetter.computeIfAbsent(letterInAlpha, HashSet::new).add(word);
        }
        dictionary.add(word);
    }

    public Set<String> getWordsWithout(char letter) {
        return doesNotHaveLetter.getOrDefault(letter, Set.of());
    }

//    public void removeWordsWithExactLetter(char letter, int position, Set<String> words) {
//        words.removeIf(word -> word.charAt(position) == letter);
//    }

    public int getDictionarySize() {
        return dictionary.size();
    }

    public List<String> getDictionary() {
        return Collections.unmodifiableList(dictionary);
    }

    public Set<String> getDictionaryCopy() {
        return new HashSet<>(dictionary);
    }
}
