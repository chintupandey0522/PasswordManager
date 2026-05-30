import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Main entry point – Swing GUI for the Smart Password Generator.
 * Covers all assignment requirements:
 *   a) Accept Name, PAN, DOB
 *   b) Generate a secure password
 *   c) Display the generated password
 *   d) Classify strength as Weak / Medium / Strong
 *   e) Test with multiple users (saved to a table)
 */
public class Main extends JFrame {

    // ── Services ────────────────────────────────────────────────
    private final PasswordGenerator      generator = new PasswordGenerator();
    private final PasswordStrengthChecker checker   = new PasswordStrengthChecker();

    // ── Input fields ─────────────────────────────────────────────
    private JTextField nameField, panField, dobField, lengthField;
    private JCheckBox  chkUpper, chkLower, chkDigits, chkSymbols;

    // ── Output ───────────────────────────────────────────────────
    private JTextField passwordField;
    private JLabel     strengthLabel;
    private JProgressBar strengthBar;

    // ── Multi-user test table ─────────────────────────────────────
    private DefaultTableModel tableModel;

    // ── State ─────────────────────────────────────────────────────
    private String currentPassword = "";
    private User   currentUser     = null;

    // ─────────────────────────────────────────────────────────────
    public Main() {
        setTitle("Smart Password Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 680);
        setMinimumSize(new Dimension(600, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 250));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildTablePanel(),  BorderLayout.SOUTH);
    }

    // ── Top: title ────────────────────────────────────────────────
    private JPanel buildTopPanel() {
        JPanel p = new JPanel();
        p.setBackground(new Color(52, 48, 140));
        p.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel("🔐  Smart Password Generator");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        p.add(title);
        return p;
    }

    // ── Center: input + result ────────────────────────────────────
    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 12, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 6, 14));
        p.add(buildInputCard());
        p.add(buildResultCard());
        return p;
    }

    private JPanel buildInputCard() {
        JPanel card = card("User Details");

        // Name
        card.add(label("Full Name"));
        nameField = new JTextField();
        card.add(nameField);

        // PAN
        card.add(label("PAN Number  (e.g. ABCDE1234F)"));
        panField = new JTextField();
        card.add(panField);

        // DOB
        card.add(label("Date of Birth  (DD/MM/YYYY)"));
        dobField = new JTextField();
        card.add(dobField);

        // Length
        card.add(label("Password Length  (8 – 32)"));
        lengthField = new JTextField("14");
        card.add(lengthField);

        // Checkboxes
        card.add(label("Include Characters"));
        JPanel chkPanel = new JPanel(new GridLayout(2, 2, 4, 4));
        chkPanel.setOpaque(false);
        chkUpper   = checkbox("Uppercase (A-Z)", true);
        chkLower   = checkbox("Lowercase (a-z)", true);
        chkDigits  = checkbox("Digits (0-9)",    true);
        chkSymbols = checkbox("Symbols (!@#…)",  true);
        chkPanel.add(chkUpper); chkPanel.add(chkLower);
        chkPanel.add(chkDigits); chkPanel.add(chkSymbols);
        card.add(chkPanel);

        card.add(new JLabel()); // spacer

        // Generate button
        JButton genBtn = new JButton("Generate Password");
        genBtn.setBackground(new Color(52, 48, 140));
        genBtn.setForeground(Color.WHITE);
        genBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        genBtn.setFocusPainted(false);
        genBtn.setBorder(new EmptyBorder(10, 16, 10, 16));
        genBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        genBtn.addActionListener(e -> onGenerate());
        card.add(genBtn);

        return card;
    }

    private JPanel buildResultCard() {
        JPanel card = card("Generated Password");

        card.add(label("Password"));
        JPanel pwdRow = new JPanel(new BorderLayout(6, 0));
        pwdRow.setOpaque(false);
        passwordField = new JTextField();
        passwordField.setEditable(false);
        passwordField.setFont(new Font("Monospaced", Font.PLAIN, 13));
        passwordField.setBackground(new Color(235, 235, 245));
        JButton copyBtn = new JButton("Copy");
        copyBtn.setFocusPainted(false);
        copyBtn.addActionListener(e -> copyPassword());
        pwdRow.add(passwordField, BorderLayout.CENTER);
        pwdRow.add(copyBtn, BorderLayout.EAST);
        card.add(pwdRow);

        card.add(label("Strength"));
        strengthLabel = new JLabel("—");
        strengthLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(strengthLabel);

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(false);
        strengthBar.setPreferredSize(new Dimension(0, 14));
        card.add(strengthBar);

        // Stats panel (filled on generate)
        card.add(label("Character Breakdown"));
        JPanel statsWrap = new JPanel(new GridLayout(2, 3, 6, 6));
        statsWrap.setOpaque(false);
        String[] statIds = {"Length","Uppercase","Lowercase","Digits","Symbols","Unique"};
        for (String s : statIds) {
            JLabel lbl = new JLabel("— " + s, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lbl.setOpaque(true);
            lbl.setBackground(new Color(230, 230, 240));
            lbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
            lbl.setName(s);
            statsWrap.add(lbl);
        }
        card.add(statsWrap);

        card.add(new JLabel()); // spacer

        JButton saveBtn = new JButton("Save to Test List ↓");
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> saveUser());
        card.add(saveBtn);

        return card;
    }

    // ── Bottom: multi-user table ───────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 14, 14, 14));

        JLabel hdr = new JLabel("  Tested Users");
        hdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        hdr.setOpaque(true);
        hdr.setBackground(new Color(200, 198, 230));
        hdr.setBorder(new EmptyBorder(6, 8, 6, 8));
        p.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Name", "PAN", "Date of Birth", "Password", "Strength"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 160));
        p.add(scroll, BorderLayout.CENTER);

        JButton clearBtn = new JButton("Clear List");
        clearBtn.addActionListener(e -> tableModel.setRowCount(0));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnRow.setOpaque(false);
        btnRow.add(clearBtn);
        p.add(btnRow, BorderLayout.SOUTH);

        return p;
    }

    // ── Actions ───────────────────────────────────────────────────
    private void onGenerate() {
        String name = nameField.getText().trim();
        String pan  = panField.getText().trim().toUpperCase();
        String dob  = dobField.getText().trim();

        // Validate
        if (name.isEmpty()) { alert("Name cannot be empty."); return; }
        if (!pan.matches("[A-Z]{5}[0-9]{4}[A-Z]")) {
            alert("PAN must be 10 characters like ABCDE1234F."); return;
        }
        if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
            alert("Date of Birth must be in DD/MM/YYYY format."); return;
        }

        int len = 14;
        try { len = Integer.parseInt(lengthField.getText().trim()); }
        catch (NumberFormatException ignored) {}
        len = Math.max(8, Math.min(32, len));

        currentUser = new User(name, pan, dob);
        currentPassword = generator.generate(
            currentUser, len,
            chkUpper.isSelected(), chkLower.isSelected(),
            chkDigits.isSelected(), chkSymbols.isSelected()
        );

        passwordField.setText(currentPassword);

        // Strength
        PasswordStrengthChecker.Strength s = checker.classify(currentPassword);
        strengthLabel.setText(checker.label(s));
        strengthBar.setValue(Math.round(checker.fillRatio(s) * 100));
        switch (s) {
            case WEAK:   strengthLabel.setForeground(new Color(180, 30, 30));
                         strengthBar.setForeground(new Color(226, 75, 74));  break;
            case MEDIUM: strengthLabel.setForeground(new Color(140, 90, 0));
                         strengthBar.setForeground(new Color(239, 159, 39)); break;
            case STRONG: strengthLabel.setForeground(new Color(40, 110, 20));
                         strengthBar.setForeground(new Color(99, 153, 34));  break;
        }

        // Stats — find the statsWrap panel inside result card
        updateStats(currentPassword);
    }

    private void updateStats(String pwd) {
        // Walk the component tree to find stat labels by name
        for (Component comp : ((JPanel) getContentPane()
                .getComponent(1))           // center panel
                .getComponent(1)            // result card
                .getParent().getComponents()) {
        }
        // Simpler: just set the password field tooltip with stats
        int upper  = pwd.replaceAll("[^A-Z]", "").length();
        int lower  = pwd.replaceAll("[^a-z]", "").length();
        int digits = pwd.replaceAll("[^0-9]", "").length();
        int syms   = pwd.replaceAll("[A-Za-z0-9]", "").length();
        long uniq  = pwd.chars().distinct().count();

        passwordField.setToolTipText(String.format(
            "Length:%d  Upper:%d  Lower:%d  Digits:%d  Symbols:%d  Unique:%d",
            pwd.length(), upper, lower, digits, syms, uniq));

        // Find all stat labels and update them
        updateStatLabels(getContentPane(), pwd);
    }

    private void updateStatLabels(Container container, String pwd) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel) {
                JLabel lbl = (JLabel) c;
                switch (lbl.getName() == null ? "" : lbl.getName()) {
                    case "Length":    lbl.setText(pwd.length() + " Length"); break;
                    case "Uppercase": lbl.setText(pwd.replaceAll("[^A-Z]","").length() + " Upper"); break;
                    case "Lowercase": lbl.setText(pwd.replaceAll("[^a-z]","").length() + " Lower"); break;
                    case "Digits":    lbl.setText(pwd.replaceAll("[^0-9]","").length() + " Digits"); break;
                    case "Symbols":   lbl.setText(pwd.replaceAll("[A-Za-z0-9]","").length() + " Symbols"); break;
                    case "Unique":    lbl.setText(pwd.chars().distinct().count() + " Unique"); break;
                }
            }
            if (c instanceof Container) updateStatLabels((Container) c, pwd);
        }
    }

    private void copyPassword() {
        if (currentPassword.isEmpty()) return;
        Toolkit.getDefaultToolkit().getSystemClipboard()
               .setContents(new StringSelection(currentPassword), null);
        JOptionPane.showMessageDialog(this, "Password copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveUser() {
        if (currentPassword.isEmpty() || currentUser == null) {
            alert("Generate a password first."); return;
        }
        PasswordStrengthChecker.Strength s = checker.classify(currentPassword);
        tableModel.addRow(new Object[]{
            currentUser.getName(),
            currentUser.getPan(),
            currentUser.getDob(),
            currentPassword,
            checker.label(s)
        });
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 210), 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));
        JLabel hdr = new JLabel(title);
        hdr.setFont(new Font("SansSerif", Font.BOLD, 14));
        hdr.setForeground(new Color(52, 48, 140));
        hdr.setAlignmentX(LEFT_ALIGNMENT);
        hdr.setBorder(new EmptyBorder(0, 0, 10, 0));
        p.add(hdr);
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(90, 90, 110));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(8, 0, 2, 0));
        return l;
    }

    private JCheckBox checkbox(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return cb;
    }

    private void alert(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    // ── Entry point ───────────────────────────────────────────────
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new Main().setVisible(true);
        });
    }
}
