package wordle;

import java.util.Scanner;

public class Wordle {

    private static final String DICTIONARY_PATH = "/dictionary.txt";
    private static final int WORD_LENGTH = 5;
    private static final int NUM_GUESSES = 6;

    private final Scanner scanner;
    private final Solver solver;

    public Wordle() {
        this.scanner = new Scanner(System.in);
        this.solver = new Solver(WORD_LENGTH, new DictionaryFileLoader(DICTIONARY_PATH));
    }

    public static void main(String[] args) {
        Wordle wordle = new Wordle();
        wordle.play();
    }

    private void play() {

        String nextWord = getFirstWord();

        int guessNumber = 1;
        Color[] colors = new Color[WORD_LENGTH];
        while (guessNumber <= NUM_GUESSES && !isSolved(colors)) {
            colors = askColorResult(nextWord);
            nextWord = solver.findNextWord(nextWord.toLowerCase(), colors);

            guessNumber++;
        }

        if (isSolved(colors)) {
            System.out.println("Nicely done!");
        } else {
            System.out.println("Better luck next time :(");
        }
    }

    private String getFirstWord() {
        if (shouldCalculateFirst()) {
            System.out.println("Calculating first word. This may take a few minutes...");
            //return solver.findNextWord();
            return "tares";
        } else {
            return promptFirstWord();
        }
    }

    private boolean shouldCalculateFirst() {
        System.out.println("Do you want me to choose your first word? (Y/N)");
        System.out.print("> ");
        String answer = scanner.nextLine();
        return answer.equals("Y") || answer.equals("y");
    }

    private String promptFirstWord() {
        while (true) {
            System.out.println("Enter your starting word");
            System.out.print("> ");
            String word = scanner.nextLine();
            if (word.length() != WORD_LENGTH || !isValidWord(word)) {
                System.out.println("Invalid word.");
            } else {
                return word;
            }
        }
    }

    // TODO: implement
    private boolean isValidWord(String word) {
        // TODO: check if alphabetical, check if in dictionary
        return true;
    }

    private Color[] askColorResult(String nextWord) {
        System.out.print("Enter the word ");
        if (nextWord != null) {
            System.out.print("'" + nextWord + "' ");
        }
        System.out.print("into the game and tell me the color result");
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
}
