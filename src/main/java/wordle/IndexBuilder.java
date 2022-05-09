package wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IndexBuilder {

    private static final String DICTIONARY_PATH = "/dictionary.txt";

    /**
     * Builds a WordIndex for the given dictionary file. The dictionary is expected to have only words of the target length;
     *
     * @param wordLength the length of words in the dictionary
     */
    public WordIndex buildIndex(int wordLength) throws IOException {
        WordIndex index = new WordIndex(wordLength);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(DICTIONARY_PATH)))) {

            String line;
            while ((line = reader.readLine()) != null) {
                index.indexWord(line);
            }
        }

        return index;
    }
}
