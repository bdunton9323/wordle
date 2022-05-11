package wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DictionaryFileLoader {

    private final String filePath;

    public DictionaryFileLoader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Builds a Dictionary for the given dictionary file. The dictionary is expected to have only words of the target
     * length;
     *
     */
    public Dictionary buildDictionary() throws IOException {
        Dictionary dictionary = new Dictionary();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(filePath)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.addWord(line);
            }
        }

        return dictionary;
    }
}
