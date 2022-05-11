package wordle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dictionary {
    private final List<String> dictionary;

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
}
