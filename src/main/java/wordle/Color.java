package wordle;

/**
 * Represents a color that can be returned from the game
 */
public enum Color {
    GREEN("G"),
    YELLOW("Y"),
    GRAY("-");

    private final String color;

    Color(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color;
    }
}
