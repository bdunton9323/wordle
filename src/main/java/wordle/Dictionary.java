package wordle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Dictionary {
    private List<String> dictionary;

    public Dictionary() {
        this(new ArrayList<>(5000));
    }

    public Dictionary(List<String> words) {
        this.dictionary = new ArrayList<>(words);
    }

    public void addWord(String word) {
        dictionary.add(word);
    }

    public int size() {
        return dictionary.size();
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(dictionary);
    }

    public void intersect(Set<String> wordsToRetain) {
        // it's inefficient, but it's only done once per guess.
        Set<String> newWords = new HashSet<>(dictionary);
        newWords.retainAll(wordsToRetain);
        dictionary = new ArrayList<>(newWords);
    }
}
