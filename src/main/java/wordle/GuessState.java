package wordle;

import java.util.Set;

public class GuessState {
    private final double reduction;
    private final Set<String> words;
    private final String pattern;

    public GuessState(double reduction, Set<String> words, Color[] outcome) {
        this.reduction = reduction;
        this.words = words;
        this.pattern = buildPattern(outcome);
    }

    public double getReduction() {
        return reduction;
    }

    public Set<String> getWords() {
        return words;
    }

    public String getPattern() {
        return pattern;
    }

    private String buildPattern(Color[] outcome) {
        StringBuilder sb = new StringBuilder();
        for (Color c : outcome) {
            sb.append(c.toString());
        }
        return sb.toString();
    }
}
