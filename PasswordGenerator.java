import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PasswordGenerator {

    private static final String UPPER   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER   = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS  = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private Random random = new Random();

    /**
     * Generates a secure password from the user's personal details.
     * Seeds characters from Name, PAN, and DOB, then fills remaining
     * length with random characters from the chosen pool.
     *
     * @param user      the User object containing Name, PAN, DOB
     * @param length    desired password length (min 8, max 32)
     * @param useUpper  include uppercase letters
     * @param useLower  include lowercase letters
     * @param useDigits include digits
     * @param useSymbols include symbols
     * @return the generated password as a String
     */
    public String generate(User user, int length,
                           boolean useUpper, boolean useLower,
                           boolean useDigits, boolean useSymbols) {

        // Build character pool
        StringBuilder pool = new StringBuilder();
        if (useUpper)   pool.append(UPPER);
        if (useLower)   pool.append(LOWER);
        if (useDigits)  pool.append(DIGITS);
        if (useSymbols) pool.append(SYMBOLS);
        if (pool.length() == 0) { pool.append(LOWER); pool.append(DIGITS); }

        List<Character> chars = new ArrayList<>();

        // --- Seed characters derived from user data ---

        // From Name: first letter uppercase, second letter lowercase
        String nameClean = user.getName().replaceAll("\\s+", "");
        if (nameClean.length() >= 1 && useUpper)
            chars.add(Character.toUpperCase(nameClean.charAt(0)));
        if (nameClean.length() >= 2 && useLower)
            chars.add(Character.toLowerCase(nameClean.charAt(1)));

        // From PAN: extract the 4 embedded digits (positions 5-8)
        String pan = user.getPan();
        if (useDigits && pan.length() >= 9) {
            for (int i = 5; i <= 8 && chars.size() < length - 2; i++) {
                chars.add(pan.charAt(i));
            }
        }

        // From DOB: extract day and year digits  e.g. "15/08/2001" → '1','5','2','0'
        String dobClean = user.getDob().replaceAll("[^0-9]", "");
        if (useDigits && dobClean.length() >= 2)
            chars.add(dobClean.charAt(0)); // tens digit of day
        if (useDigits && dobClean.length() >= 4)
            chars.add(dobClean.charAt(6)); // year first digit

        // Guarantee at least one symbol
        if (useSymbols)
            chars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));

        // --- Fill remaining positions randomly from pool ---
        String poolStr = pool.toString();
        while (chars.size() < length) {
            chars.add(poolStr.charAt(random.nextInt(poolStr.length())));
        }

        // Trim to exact length, shuffle, return
        while (chars.size() > length) chars.remove(chars.size() - 1);
        Collections.shuffle(chars, random);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
    }
}
