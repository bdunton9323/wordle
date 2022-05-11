package wordle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DictionaryPreparer {

    private static final int WORD_LENGTH = 5;

    /**
     * Loads a dictionary file, filters it down to just the words of the right length, and writes it back out.
     *
     * @param inputFile the raw dictionary
     * @param outputFile the trimmed dictionary
     * @throws IOException if the file could not be loaded
     */
    public void prepare(String inputFile, String outputFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line = reader.readLine();

            while (line != null) {

                if (shouldInclude(line)) {
                    writer.write(line);
                    writer.newLine();
                }

                line = reader.readLine();
            }
        }
    }

    private boolean shouldInclude(String word) {
        // the dictionary contains proper nouns and possessive words (e.g. "newsprint's")
        return word.length() == WORD_LENGTH
                && !Character.isUpperCase(word.charAt(0))
                && word.charAt(3) != '\''
                && checkLetters(word);
    }

    private boolean checkLetters(String word) {
        for (char c : word.toCharArray()) {
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        DictionaryPreparer prep = new DictionaryPreparer();
        prep.prepare("/usr/share/dict/american-english", "/tmp/wordlesolver/dictionary.txt");
    }
}
