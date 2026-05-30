public class PasswordStrengthChecker {

    public enum Strength { WEAK, MEDIUM, STRONG }

    /**
     * Classifies the strength of the given password.
     *
     * Scoring criteria (1 point each):
     *   1. Length >= 10
     *   2. Length >= 14
     *   3. Contains uppercase letter
     *   4. Contains lowercase letter
     *   5. Contains digit
     *   6. Contains symbol
     *   7. Unique characters >= 8
     *   8. Unique characters >= 12
     *
     * Score 0-3  → WEAK
     * Score 4-5  → MEDIUM
     * Score 6-8  → STRONG
     */
    public Strength classify(String password) {
        int score = 0;

        if (password.length() >= 10) score++;
        if (password.length() >= 14) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;

        long uniqueCount = password.chars().distinct().count();
        if (uniqueCount >= 8)  score++;
        if (uniqueCount >= 12) score++;

        if (score <= 3) return Strength.WEAK;
        if (score <= 5) return Strength.MEDIUM;
        return Strength.STRONG;
    }

    /** Returns a human-readable label with an emoji indicator. */
    public String label(Strength s) {
        switch (s) {
            case WEAK:   return "⚠ Weak";
            case MEDIUM: return "◑ Medium";
            case STRONG: return "✔ Strong";
            default:     return "Unknown";
        }
    }

    /** Returns the fill proportion for a strength bar (0.0 – 1.0). */
    public float fillRatio(Strength s) {
        switch (s) {
            case WEAK:   return 0.25f;
            case MEDIUM: return 0.60f;
            case STRONG: return 0.95f;
            default:     return 0f;
        }
    }
}
