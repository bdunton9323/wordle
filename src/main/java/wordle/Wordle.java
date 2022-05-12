package wordle;

import java.io.IOException;
import java.util.Scanner;

/**
 * The entrypoint for the Wordle solver.
 */
public class Wordle {

    private static final String DICTIONARY_PATH = "/dictionary.txt";
    private static final int WORD_LENGTH = 5;
    private static final int NUM_GUESSES = 6;

    private final Scanner scanner;
    private final Solver solver;
    private final Dictionary dictionary;

    public Wordle() throws IOException {
        this.scanner = new Scanner(System.in);

        dictionary = new DictionaryFileLoader(DICTIONARY_PATH).buildDictionary();
        WordMatcher wordMatcher = new WordMatcher(dictionary);
        GoodnessCalculator goodnessCalculator = new GoodnessCalculator(WORD_LENGTH, wordMatcher,
                new EntropyCalculator());
        this.solver = new Solver(WORD_LENGTH, dictionary, wordMatcher, goodnessCalculator);

        System.out.println("Dictionary has " + dictionary.size() + " " + WORD_LENGTH + "-letter words");
    }

    /**
     * Entrypoint. No arguments expected.
     */
    public static void main(String[] args) throws IOException {
        Wordle wordle = new Wordle();
        wordle.play();
    }

    private void play() {
        showBanner();
        String nextWord = getFirstWord();

        int guessNumber = 1;
        Color[] colors = new Color[WORD_LENGTH];
        while (guessNumber <= NUM_GUESSES && nextWord != null) {
            colors = askColorResult(nextWord);
            if (isSolved(colors)) {
                break;
            }
            nextWord = solver.findNextWord(nextWord.toLowerCase(), colors);

            guessNumber++;
        }

        if (nextWord == null) {
            System.out.println("Something went wrong. I could not solve this puzzle.");
        } else if (isSolved(colors)) {
            System.out.println("Nicely done!");
        } else {
            System.out.println("Better luck next time :(");
        }
    }

    private String getFirstWord() {
        if (shouldCalculateFirst()) {
            System.out.println("Calculating first word. This may take a few minutes...");
            return solver.findFirstWord();
        } else {
            return promptFirstWord();
        }
    }

    private boolean shouldCalculateFirst() {
        System.out.println("Do you want me to choose an optimal first word? This may take several minutes. (Y/N)");
        System.out.println("    (Note: the optimal word will be consistent for a given dictionary, so if you know the word then skip this)");
        System.out.print("> ");
        String answer = scanner.nextLine();
        return answer.equals("Y") || answer.equals("y");
    }

    private String promptFirstWord() {
        while (true) {
            System.out.println("Enter your starting word");
            System.out.print("> ");
            String word = scanner.nextLine().toLowerCase();
            if (word.length() != WORD_LENGTH || !isValidWord(word)) {
                System.out.println("Invalid word.");
            } else {
                return word;
            }
        }
    }

    private boolean isValidWord(String word) {
        return word.matches("^[a-z]*$") && dictionary.getWords().contains(word);
    }

    private Color[] askColorResult(String nextWord) {
        System.out.print("Enter the word ");
        if (nextWord != null) {
            System.out.print("'" + nextWord + "' ");
        }
        System.out.println("into the game and tell me the color result");
        System.out.println("Use 'G' for green, 'Y' for yellow, and '-' for gray, with no spaces in between");
        System.out.println("Example: --Y-G");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();
            try {
                return toColorArray(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Sorry, " + e.getMessage() + ". Please enter the color output again.");
            }
        }

    }

    private boolean isSolved(Color[] colors) {
        for (Color c : colors) {
            if (c != Color.GREEN) {
                return false;
            }
        }
        return true;
    }

    private Color[] toColorArray(String line) {
        char[] entry = line.toCharArray();
        if (entry.length != WORD_LENGTH) {
            throw new IllegalArgumentException("exactly " + WORD_LENGTH + " colors are required");
        }

        Color[] colors = new Color[WORD_LENGTH];
        for (int i = 0; i < entry.length; i++) {
            switch (entry[i]) {
                case 'g':
                    // fall through
                case 'G':
                    colors[i] = Color.GREEN;
                    break;
                case 'y':
                    // fall through
                case 'Y':
                    colors[i] = Color.YELLOW;
                    break;
                case '-':
                    colors[i] = Color.GRAY;
                    break;
                default:
                    throw new IllegalArgumentException("'" + entry[i] + "' is not a valid color");
            }
        }
        return colors;
    }

    private void showBanner() {
        String banner = "\n" +
                ".---------.                         .---------.\n" +
                "| _    _  |                   _  _  |         |   _____         _                    \n" +
                "|| |  | | |                  | || | |         |  /  ___|       | |                   \n" +
                "|| |  | | |   ___   _ __   __| || | |   ___   |  \\ `--.   ___  | |__   __  ___  _ __ \n" +
                "|| |/\\| | |  / _ \\ | '__| / _` || | |  / _ \\  |   `--. \\ / _ \\ | |\\ \\ / / / _ \\| '__|\n" +
                "|\\  /\\  / | | (_) || |   | (_| || | | |  __/  |  /\\__/ /| (_) || | \\ V / |  __/| |   \n" +
                "| \\/  \\/  |  \\___/ |_|    \\__,_||_| |  \\___|  |  \\____/  \\___/ |_|  \\_/   \\___||_|   \n" +
                "'---------'                         '---------'                                            \n" +
                "                                                                                 \n" +
                "\n";
        System.out.println(banner);
    }
}
