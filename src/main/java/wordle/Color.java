package wordle;

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
