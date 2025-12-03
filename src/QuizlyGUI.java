import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class QuizlyGUI extends JFrame {
    
    // --- PALET WARNA (DARK GRADIENT THEME) ---
    final Color COL_GRADIENT_START = Color.decode("#141E30"); 
    final Color COL_GRADIENT_END   = Color.decode("#243B55"); 
    
    final Color COL_BTN_START      = Color.decode("#4e54c8"); // Ungu Modern
    final Color COL_BTN_END        = Color.decode("#8f94fb"); // Biru Muda
    
    final Color COL_DANGER         = Color.decode("#e74c3c"); 
    final Color COL_SUCCESS        = Color.decode("#2ecc71"); 
    final Color COL_TEXT_DARK      = Color.decode("#2c3e50"); 
    final Color COL_FIELD_BG       = Color.decode("#f1f2f6");
    final Color COL_TABLE_HEAD     = Color.decode("#4e54c8");

    // --- FONTS ---
    final Font FONT_TITLE_BIG = new Font("Segoe UI", Font.BOLD, 28);
    final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 22);
    final Font FONT_NORMAL    = new Font("Segoe UI", Font.PLAIN, 14);
    final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 14);

    // Panel Management
    private JPanel mainPanel;
    private CardLayout cardLayout;
    User currentUser; 

    public QuizlyGUI() {
        setTitle("Quizly - Interactive Quiz App");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Init Database
        DatabaseHelper.createDatabaseIfNotExists();
        DatabaseHelper.createTables();

        // 2. Setup Layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 3. Add Screens
        mainPanel.add(new LoginPanel(), "LOGIN");
        mainPanel.add(new RegisterPanel(), "REGISTER");
        
        add(mainPanel);
        setVisible(true); 
    }

    // --- HELPER METHODS ---
    public void switchCard(String cardName) {
        cardLayout.show(mainPanel, cardName);
    }

    public void setUserSession(User user) {
        this.currentUser = user;
        if (user instanceof Teacher) {
            mainPanel.add(new TeacherDashboard((Teacher) user), "TEACHER_DASH");
            switchCard("TEACHER_DASH");
        } else if (user instanceof Student) {
            mainPanel.add(new StudentDashboard((Student) user), "STUDENT_DASH");
            switchCard("STUDENT_DASH");
        }
    }

    // --- HELPER: STYLE TABEL MODERN (WHITE/LIGHT) ---
    private void styleModernTable(JTable table) {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(232, 240, 254)); 
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(Color.WHITE);
        table.setForeground(COL_TEXT_DARK);
        
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer((t, value, isSelected, hasFocus, row, col) -> {
            JLabel l = new JLabel(value.toString());
            l.setOpaque(true); l.setBackground(COL_TABLE_HEAD); l.setForeground(Color.WHITE);
            l.setFont(new Font("Segoe UI", Font.BOLD, 14)); l.setHorizontalAlignment(JLabel.CENTER);
            l.setBorder(new EmptyBorder(12, 10, 12, 10)); return l;
        });
        header.setPreferredSize(new Dimension(0, 50));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) { 
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250)); 
                    c.setForeground(COL_TEXT_DARK);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10)); setHorizontalAlignment(JLabel.CENTER); return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    // ==================================================================================
    // CUSTOM COMPONENT MODERN
    // ==================================================================================
    
    class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, COL_GRADIENT_START, getWidth(), getHeight(), COL_GRADIENT_END);
            g2d.setPaint(gp); g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    class RoundedGradientButton extends JButton {
        Color c1, c2; int radius = 30;
        public RoundedGradientButton(String text, Color start, Color end) {
            super(text); this.c1 = start; this.c2 = end;
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setForeground(Color.WHITE); setFont(FONT_BOLD); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public RoundedGradientButton(String text) { this(text, COL_BTN_START, COL_BTN_END); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2d.setPaint(gp); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2d.dispose(); super.paintComponent(g);
        }
    }

    class RoundedTextField extends JTextField {
        public RoundedTextField(int cols) { super(cols); setOpaque(false); setFont(FONT_NORMAL); setBorder(new EmptyBorder(10, 15, 10, 15)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COL_FIELD_BG); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20); super.paintComponent(g2);
        }
        @Override protected void paintBorder(Graphics g) {}
    }
    
    class RoundedPasswordField extends JPasswordField {
        public RoundedPasswordField(int cols) { super(cols); setOpaque(false); setFont(FONT_NORMAL); setBorder(new EmptyBorder(10, 15, 10, 15)); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COL_FIELD_BG); g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20); super.paintComponent(g2);
        }
        @Override protected void paintBorder(Graphics g) {}
    }

    private JPanel createWhiteCard() {
        JPanel card = new JPanel(new GridBagLayout()); card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,30), 1, true), new EmptyBorder(40, 50, 40, 50)));
        return card;
    }

    // ==================================================================================
    // 1. LOGIN PANEL
    // ==================================================================================
    class LoginPanel extends GradientPanel {
        JTextField userField; JPasswordField passField;

        public LoginPanel() {
            setLayout(new GridBagLayout()); JPanel card = createWhiteCard();
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 0, 10, 0); gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Welcome Back!", SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK); card.add(title, gbc);
            gbc.gridy++; JLabel sub = new JLabel("Login to Quizly App", SwingConstants.CENTER); sub.setFont(FONT_NORMAL); sub.setForeground(Color.GRAY); card.add(sub, gbc);

            gbc.gridy++; gbc.insets = new Insets(20, 0, 5, 0); JLabel lblU = new JLabel("Username"); lblU.setFont(FONT_BOLD); lblU.setForeground(COL_TEXT_DARK); card.add(lblU, gbc);
            gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0); userField = new RoundedTextField(20); userField.setPreferredSize(new Dimension(300, 45)); card.add(userField, gbc);

            gbc.gridy++; gbc.insets = new Insets(5, 0, 5, 0); JLabel lblP = new JLabel("Password"); lblP.setFont(FONT_BOLD); lblP.setForeground(COL_TEXT_DARK); card.add(lblP, gbc);
            gbc.gridy++; gbc.insets = new Insets(0, 0, 25, 0); passField = new RoundedPasswordField(20); passField.setPreferredSize(new Dimension(300, 45)); card.add(passField, gbc);

            gbc.gridy++; RoundedGradientButton loginBtn = new RoundedGradientButton("LOGIN"); loginBtn.setPreferredSize(new Dimension(300, 45));
            loginBtn.addActionListener(e -> handleLogin()); card.add(loginBtn, gbc);

            gbc.gridy++; gbc.insets = new Insets(15, 0, 0, 0);
            JLabel regLink = new JLabel("<html>No account? <font color='#4e54c8'><b>Register here</b></font></html>", SwingConstants.CENTER);
            regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            regLink.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchCard("REGISTER"); } });
            card.add(regLink, gbc);
            add(card);
        }

        private void handleLogin() {
            User user = User.login(userField.getText(), new String(passField.getPassword()));
            if (user != null) { setUserSession(user); userField.setText(""); passField.setText(""); } 
            else { JOptionPane.showMessageDialog(this, "Invalid Username/Password"); }
        }
    }

    // ==================================================================================
    // 2. REGISTER PANEL
    // ==================================================================================
    class RegisterPanel extends GradientPanel {
        JTextField userField; JPasswordField passField; JComboBox<String> roleBox;

        public RegisterPanel() {
            setLayout(new GridBagLayout()); JPanel card = createWhiteCard();
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 0, 10, 0); gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Create Account", SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK); card.add(title, gbc);
            gbc.gridy++; gbc.insets = new Insets(20, 0, 5, 0); card.add(new JLabel("Username"), gbc);
            gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0); userField = new RoundedTextField(20); userField.setPreferredSize(new Dimension(300, 45)); card.add(userField, gbc);
            gbc.gridy++; gbc.insets = new Insets(5, 0, 5, 0); card.add(new JLabel("Password"), gbc);
            gbc.gridy++; gbc.insets = new Insets(0, 0, 10, 0); passField = new RoundedPasswordField(20); passField.setPreferredSize(new Dimension(300, 45)); card.add(passField, gbc);
            gbc.gridy++; gbc.insets = new Insets(5, 0, 5, 0); card.add(new JLabel("Role"), gbc);
            gbc.gridy++; gbc.insets = new Insets(0, 0, 25, 0);
            roleBox = new JComboBox<>(new String[]{"STUDENT", "TEACHER"}); roleBox.setPreferredSize(new Dimension(300, 40)); roleBox.setBackground(Color.WHITE);
            card.add(roleBox, gbc);

            gbc.gridy++; RoundedGradientButton regBtn = new RoundedGradientButton("REGISTER"); regBtn.setPreferredSize(new Dimension(300, 45));
            regBtn.addActionListener(e -> handleRegister()); card.add(regBtn, gbc);

            gbc.gridy++; gbc.insets = new Insets(15, 0, 0, 0);
            JLabel loginLink = new JLabel("<html>Have account? <font color='#4e54c8'><b>Login here</b></font></html>", SwingConstants.CENTER);
            loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginLink.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchCard("LOGIN"); } });
            card.add(loginLink, gbc);
            add(card);
        }

        private void handleRegister() {
            if(User.register(userField.getText(), new String(passField.getPassword()), (String) roleBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "Registration Successful!"); switchCard("LOGIN");
            } else { JOptionPane.showMessageDialog(this, "Username exists!"); }
        }
    }

    // ==================================================================================
    // 3. TEACHER DASHBOARD
    // ==================================================================================
    class TeacherDashboard extends GradientPanel {
        public TeacherDashboard(Teacher teacher) {
            setLayout(new GridBagLayout()); JPanel card = createWhiteCard();
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(15, 0, 15, 0); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Teacher Dashboard", SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK);
            JLabel subtitle = new JLabel("Welcome, " + teacher.getUsername(), SwingConstants.CENTER); subtitle.setFont(FONT_NORMAL); subtitle.setForeground(Color.GRAY);

            Dimension btnSize = new Dimension(350, 50);
            RoundedGradientButton createBtn = new RoundedGradientButton("Create New Quiz"); createBtn.setPreferredSize(btnSize);
            RoundedGradientButton viewBtn = new RoundedGradientButton("My Quizzes & Leaderboard", COL_SUCCESS, COL_SUCCESS.darker()); viewBtn.setPreferredSize(btnSize);
            RoundedGradientButton logoutBtn = new RoundedGradientButton("Logout", COL_DANGER, COL_DANGER.darker()); logoutBtn.setPreferredSize(btnSize);

            gbc.gridy = 0; card.add(title, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 30, 0); card.add(subtitle, gbc);
            gbc.gridy = 2; gbc.insets = new Insets(10, 0, 10, 0); card.add(createBtn, gbc);
            gbc.gridy = 3; card.add(viewBtn, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(30, 0, 0, 0); card.add(logoutBtn, gbc);

            add(card);
            logoutBtn.addActionListener(e -> switchCard("LOGIN"));
            createBtn.addActionListener(e -> { mainPanel.add(new CreateQuizPanel(teacher), "CREATE"); switchCard("CREATE"); });
            viewBtn.addActionListener(e -> { mainPanel.add(new ViewQuizPanel(teacher), "VIEW_QUIZ"); switchCard("VIEW_QUIZ"); });
        }
    }

    // ==================================================================================
    // 4. STUDENT DASHBOARD
    // ==================================================================================
    class StudentDashboard extends GradientPanel {
        public StudentDashboard(Student student) {
            setLayout(new GridBagLayout()); JPanel card = createWhiteCard();
            GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Student Dashboard", SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK);
            JLabel subtitle = new JLabel("Logged in as: " + student.getUsername(), SwingConstants.CENTER); subtitle.setFont(FONT_NORMAL); subtitle.setForeground(Color.GRAY);

            RoundedTextField keyField = new RoundedTextField(20); keyField.setPreferredSize(new Dimension(350, 50));
            keyField.setHorizontalAlignment(JTextField.CENTER); keyField.setText("ENTER QUIZ KEY HERE"); keyField.setForeground(Color.GRAY);
            keyField.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { if(keyField.getText().equals("ENTER QUIZ KEY HERE")){keyField.setText(""); keyField.setForeground(Color.BLACK);} }
                public void focusLost(FocusEvent e) { if(keyField.getText().isEmpty()){keyField.setText("ENTER QUIZ KEY HERE"); keyField.setForeground(Color.GRAY);} }
            });

            RoundedGradientButton joinBtn = new RoundedGradientButton("Start Quiz"); joinBtn.setPreferredSize(new Dimension(350, 50));
            RoundedGradientButton historyBtn = new RoundedGradientButton("View My Grades", new Color(230, 126, 34), new Color(211, 84, 0)); historyBtn.setPreferredSize(new Dimension(350, 50));
            RoundedGradientButton logoutBtn = new RoundedGradientButton("Logout", COL_DANGER, COL_DANGER.darker()); logoutBtn.setPreferredSize(new Dimension(350, 50));

            gbc.gridy = 0; gbc.insets = new Insets(10, 0, 5, 0); card.add(title, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 30, 0); card.add(subtitle, gbc);
            gbc.gridy = 2; gbc.insets = new Insets(10, 0, 10, 0); card.add(keyField, gbc);
            gbc.gridy = 3; card.add(joinBtn, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(20, 0, 20, 0); JSeparator sep = new JSeparator(); sep.setForeground(Color.LIGHT_GRAY); card.add(sep, gbc);
            gbc.gridy = 5; gbc.insets = new Insets(0, 0, 10, 0); card.add(historyBtn, gbc);
            gbc.gridy = 6; gbc.insets = new Insets(10, 0, 0, 0); card.add(logoutBtn, gbc);

            add(card);
            logoutBtn.addActionListener(e -> switchCard("LOGIN"));
            historyBtn.addActionListener(e -> { mainPanel.add(new ViewScorePanel(student), "SCORE"); switchCard("SCORE"); });
            joinBtn.addActionListener(e -> {
                String key = keyField.getText().trim().toUpperCase();
                if(key.equals("ENTER QUIZ KEY HERE") || key.isEmpty()) return;
                Quiz quiz = student.joinQuiz(key);
                if (quiz != null) { mainPanel.add(new DoQuizPanel(student, quiz), "DO"); switchCard("DO"); } 
                else { JOptionPane.showMessageDialog(this, "Quiz not found or inactive!", "Error", JOptionPane.ERROR_MESSAGE); }
            });
        }
    }

    // ==================================================================================
    // 5. CREATE QUIZ PANEL
    // ==================================================================================
    class CreateQuizPanel extends GradientPanel {
        List<Question> questions = new ArrayList<>();
        JTextField titleField = new RoundedTextField(20); 
        JTextField timeField = new RoundedTextField(5);
        JTextArea qTextArea = new JTextArea(3, 20); 
        JTextField opA = new RoundedTextField(15); JTextField opB = new RoundedTextField(15); 
        JTextField opC = new RoundedTextField(15); JTextField opD = new RoundedTextField(15);
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        public CreateQuizPanel(Teacher teacher) {
            setLayout(new BorderLayout());

            JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); header.setOpaque(false);
            JLabel lblTitle = new JLabel("Quiz Title:"); lblTitle.setForeground(Color.WHITE); lblTitle.setFont(FONT_BOLD);
            JLabel lblTime = new JLabel("Time (s):"); lblTime.setForeground(Color.WHITE); lblTime.setFont(FONT_BOLD);
            titleField.setPreferredSize(new Dimension(300, 40)); timeField.setPreferredSize(new Dimension(80, 40)); timeField.setHorizontalAlignment(JTextField.CENTER); timeField.setText("30");
            header.add(lblTitle); header.add(titleField); header.add(lblTime); header.add(timeField);
            add(header, BorderLayout.NORTH);

            JPanel formCard = new JPanel(new GridBagLayout()); formCard.setBackground(Color.WHITE);
            formCard.setBorder(new CompoundBorder(new LineBorder(new Color(255,255,255,50), 1, true), new EmptyBorder(20, 40, 20, 40)));
            JPanel centerWrapper = new JPanel(new GridBagLayout()); centerWrapper.setOpaque(false); centerWrapper.add(formCard);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.gridy = 0;

            JLabel lblQ = new JLabel("Question Text"); lblQ.setFont(new Font("Segoe UI", Font.BOLD, 12)); lblQ.setForeground(COL_TEXT_DARK);
            formCard.add(lblQ, gbc);
            gbc.gridy++; gbc.gridwidth = 2;
            qTextArea.setLineWrap(true); qTextArea.setWrapStyleWord(true); qTextArea.setFont(FONT_NORMAL);
            qTextArea.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Color.LIGHT_GRAY), new EmptyBorder(10,10,10,10)));
            JScrollPane qScroll = new JScrollPane(qTextArea); qScroll.setPreferredSize(new Dimension(500, 80));
            formCard.add(qScroll, gbc);

            gbc.gridy++; gbc.gridwidth = 1; gbc.insets = new Insets(15, 10, 5, 10);
            formCard.add(createLabel("Option A (Red)"), gbc); gbc.gridx = 1; formCard.add(createLabel("Option B (Blue)"), gbc);
            gbc.gridy++; gbc.gridx = 0; gbc.insets = new Insets(0, 10, 10, 10);
            formCard.add(opA, gbc); gbc.gridx = 1; formCard.add(opB, gbc);

            gbc.gridy++; gbc.gridx = 0; gbc.insets = new Insets(5, 10, 5, 10);
            formCard.add(createLabel("Option C (Yellow)"), gbc); gbc.gridx = 1; formCard.add(createLabel("Option D (Green)"), gbc);
            gbc.gridy++; gbc.gridx = 0; gbc.insets = new Insets(0, 10, 20, 10);
            formCard.add(opC, gbc); gbc.gridx = 1; formCard.add(opD, gbc);

            gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 1; gbc.insets = new Insets(0, 10, 0, 10);
            formCard.add(createLabel("Correct Answer:"), gbc);
            gbc.gridx = 1; correctBox.setPreferredSize(new Dimension(100, 35)); correctBox.setBackground(Color.WHITE); formCard.add(correctBox, gbc);

            gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; gbc.insets = new Insets(20, 10, 0, 10);
            RoundedGradientButton addQBtn = new RoundedGradientButton("Save Question to Draft", COL_BTN_START, COL_BTN_END);
            formCard.add(addQBtn, gbc);
            add(centerWrapper, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); footer.setOpaque(false); footer.setBorder(new EmptyBorder(10, 10, 20, 10));
            JLabel countLabel = new JLabel("Draft: 0 Questions"); countLabel.setForeground(Color.WHITE); countLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            RoundedGradientButton finishBtn = new RoundedGradientButton("Upload Quiz", COL_SUCCESS, COL_SUCCESS.darker()); finishBtn.setPreferredSize(new Dimension(180, 45));
            RoundedGradientButton backBtn = new RoundedGradientButton("Cancel", COL_DANGER, COL_DANGER.darker()); backBtn.setPreferredSize(new Dimension(120, 45));
            footer.add(backBtn); footer.add(Box.createHorizontalStrut(20)); footer.add(countLabel); footer.add(Box.createHorizontalStrut(20)); footer.add(finishBtn); 
            add(footer, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> switchCard("TEACHER_DASH"));
            addQBtn.addActionListener(e -> {
                if(qTextArea.getText().trim().isEmpty() || opA.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Please fill all fields!"); return; }
                questions.add(new Question(qTextArea.getText(), opA.getText(), opB.getText(), opC.getText(), opD.getText(), (String)correctBox.getSelectedItem()));
                countLabel.setText("Draft: " + questions.size() + " Questions");
                JOptionPane.showMessageDialog(this, "Question Added!");
                qTextArea.setText(""); opA.setText(""); opB.setText(""); opC.setText(""); opD.setText(""); qTextArea.requestFocus();
            });
            finishBtn.addActionListener(e -> {
                if(questions.isEmpty()) { JOptionPane.showMessageDialog(this, "Add at least one question!"); return; }
                try {
                    int time = Integer.parseInt(timeField.getText());
                    Quiz q = teacher.createQuiz(titleField.getText(), time, questions);
                    if(q != null) { JOptionPane.showMessageDialog(this, "Quiz Created! Code: " + q.getQuizKey()); switchCard("TEACHER_DASH"); }
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Time!"); }
            });
        }
        private JLabel createLabel(String text) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(Color.GRAY); return l; }
    }

    // ==================================================================================
    // 6. VIEW QUIZ PANEL (WITH DARK BACKGROUND & WHITE CARD)
    // ==================================================================================
    class ViewQuizPanel extends GradientPanel {
        JTable table; DefaultTableModel model; Teacher teacher;

        public ViewQuizPanel(Teacher teacher) {
            this.teacher = teacher;
            setLayout(new BorderLayout()); 
            // 1. Set Padding on MAIN PANEL so the background shows through around the card
            setBorder(new EmptyBorder(30, 40, 30, 40)); 

            // 2. The Card is WHITE
            JPanel contentCard = new JPanel(new BorderLayout()); 
            contentCard.setBackground(Color.WHITE);
            // No border needed on card itself (or minimal)

            JLabel title = new JLabel("Manage My Quizzes", SwingConstants.CENTER);
            title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK); title.setBorder(new EmptyBorder(20, 0, 20, 0));
            contentCard.add(title, BorderLayout.NORTH);

            String[] cols = {"ID", "Title", "Key", "Time (s)", "Status", "Actions"};
            model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return column == 5; } };

            table = new JTable(model);
            styleModernTable(table);

            table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
            table.getColumnModel().getColumn(5).setPreferredWidth(280);

            refreshTableData();

            JScrollPane scrollPane = new JScrollPane(table); scrollPane.setBorder(null); scrollPane.getViewport().setBackground(Color.WHITE);
            contentCard.add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); footer.setBackground(Color.WHITE);
            RoundedGradientButton openBtn = new RoundedGradientButton("Open Leaderboard", COL_SUCCESS, COL_SUCCESS.darker()); openBtn.setPreferredSize(new Dimension(200, 45));
            RoundedGradientButton backBtn = new RoundedGradientButton("Back to Dashboard", COL_DANGER, COL_DANGER.darker()); backBtn.setPreferredSize(new Dimension(200, 45));
            footer.add(openBtn); footer.add(backBtn); 
            contentCard.add(footer, BorderLayout.SOUTH);

            add(contentCard, BorderLayout.CENTER); 

            backBtn.addActionListener(e -> switchCard("TEACHER_DASH"));
            openBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int quizId = (int) model.getValueAt(row, 0);
                    String quizTitle = (String) model.getValueAt(row, 1);
                    mainPanel.add(new LeaderboardPanel(quizId, quizTitle), "LEADERBOARD"); switchCard("LEADERBOARD");
                } else JOptionPane.showMessageDialog(this, "Select a quiz row first!");
            });
        }

        private void refreshTableData() {
            model.setRowCount(0);
            List<Quiz> quizList = teacher.viewMyQuizzes();
            for(Quiz q : quizList) model.addRow(new Object[]{q.getIdQuiz(), q.getTitle(), q.getQuizKey(), q.getTimeLimit(), q.getStatus(), ""});
        }

        class ButtonRenderer extends javax.swing.table.DefaultTableCellRenderer {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
                panel.setBackground(isSelected ? new Color(232, 240, 254) : (row % 2 == 0 ? Color.WHITE : new Color(248, 249, 250)));
                JButton statusBtn = new JButton("On/Off"); styleMiniButton(statusBtn, new Color(142, 68, 173));
                JButton editBtn = new JButton("Edit"); styleMiniButton(editBtn, new Color(243, 156, 18));
                JButton delBtn = new JButton("Del"); styleMiniButton(delBtn, COL_DANGER);
                panel.add(statusBtn); panel.add(editBtn); panel.add(delBtn);
                return panel;
            }
        }

        class ButtonEditor extends DefaultCellEditor {
            protected JPanel panel; protected JButton statusBtn, editBtn, delBtn; private int currentRow;
            public ButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); panel.setBackground(Color.WHITE);
                statusBtn = new JButton("On/Off"); styleMiniButton(statusBtn, new Color(142, 68, 173));
                editBtn = new JButton("Edit"); styleMiniButton(editBtn, new Color(243, 156, 18));
                delBtn = new JButton("Del"); styleMiniButton(delBtn, COL_DANGER);
                panel.add(statusBtn); panel.add(editBtn); panel.add(delBtn);

                statusBtn.addActionListener(e -> {
                    fireEditingStopped();
                    int quizId = (int) table.getValueAt(currentRow, 0);
                    String currentStatus = (String) table.getValueAt(currentRow, 4);
                    try {
                        Connection conn = DatabaseHelper.getConnection();
                        conn.prepareStatement("UPDATE quiz SET status='" + (currentStatus.equals("ACTIVE") ? "INACTIVE" : "ACTIVE") + "' WHERE id_quiz=" + quizId).executeUpdate();
                        refreshTableData();
                    } catch (Exception ex) { ex.printStackTrace(); }
                });
                editBtn.addActionListener(e -> {
                    fireEditingStopped();
                    int quizId = (int) table.getValueAt(currentRow, 0);
                    String title = (String) table.getValueAt(currentRow, 1);
                    int time = (int) table.getValueAt(currentRow, 3);
                    mainPanel.add(new EditQuizPanel(teacher, quizId, title, time), "EDIT_QUIZ"); switchCard("EDIT_QUIZ");
                });
                delBtn.addActionListener(e -> {
                    fireEditingStopped();
                    int quizId = (int) table.getValueAt(currentRow, 0);
                    if (JOptionPane.showConfirmDialog(panel, "Delete Quiz ID: " + quizId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        try {
                            Connection conn = DatabaseHelper.getConnection();
                            conn.prepareStatement("DELETE FROM quiz WHERE id_quiz=" + quizId).executeUpdate();
                            refreshTableData();
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                });
            }
            @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { currentRow = row; panel.setBackground(table.getSelectionBackground()); return panel; }
            @Override public Object getCellEditorValue() { return ""; }
        }
        private void styleMiniButton(JButton btn, Color color) {
            btn.setBackground(color); btn.setForeground(Color.WHITE); btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(75, 35)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    // ==================================================================================
    // 7. DO QUIZ PANEL (DARK GAME MODE)
    // ==================================================================================
    class DoQuizPanel extends GradientPanel {
        Student student; Quiz quiz; int idx = 0; int sessionId = 0;
        JLabel qLabel = new JLabel(); JLabel timerLabel = new JLabel("0s"); JLabel progressLabel = new JLabel();
        JButton btnA = new JButton(); JButton btnB = new JButton(); JButton btnC = new JButton(); JButton btnD = new JButton();
        final Color COL_A = new Color(231, 76, 60); final Color COL_B = new Color(41, 128, 185);
        final Color COL_C = new Color(241, 196, 15); final Color COL_D = new Color(39, 174, 96);
        Thread timerThread; boolean isRunning = false; int timeLeft; Clip bgmClip;

        public DoQuizPanel(Student student, Quiz quiz) {
            this.student = student; this.quiz = quiz;
            setLayout(new BorderLayout()); 

            JPanel topPanel = new JPanel(new BorderLayout()); topPanel.setOpaque(false); topPanel.setBorder(new EmptyBorder(20, 30, 0, 30));
            progressLabel.setFont(FONT_BOLD); progressLabel.setForeground(Color.WHITE);
            timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 30)); timerLabel.setForeground(Color.WHITE);
            topPanel.add(progressLabel, BorderLayout.WEST); topPanel.add(timerLabel, BorderLayout.EAST); add(topPanel, BorderLayout.NORTH);

            JPanel qPanel = new JPanel(new BorderLayout()); qPanel.setOpaque(false); qPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
            qLabel.setHorizontalAlignment(SwingConstants.CENTER); qLabel.setFont(new Font("Segoe UI", Font.BOLD, 28)); qLabel.setForeground(Color.WHITE);
            qPanel.add(qLabel, BorderLayout.CENTER); add(qPanel, BorderLayout.CENTER);

            JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15)); gridPanel.setOpaque(false); gridPanel.setBorder(new EmptyBorder(20, 20, 30, 20)); gridPanel.setPreferredSize(new Dimension(0, 300));
            styleOptionButton(btnA, COL_A); styleOptionButton(btnB, COL_B); styleOptionButton(btnC, COL_C); styleOptionButton(btnD, COL_D);
            gridPanel.add(btnA); gridPanel.add(btnB); gridPanel.add(btnC); gridPanel.add(btnD); add(gridPanel, BorderLayout.SOUTH);

            startDBSession(); loadQ(0); playAudio("bgm.wav", true);
            btnA.addActionListener(e -> process("A")); btnB.addActionListener(e -> process("B"));
            btnC.addActionListener(e -> process("C")); btnD.addActionListener(e -> process("D"));
        }

        private void playAudio(String filename, boolean loop) {
            try {
                String path = "assets/" + filename; File f = new File(path);
                if (!f.exists()) { path = "../assets/" + filename; f = new File(path); }
                if (f.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
                    Clip clip = AudioSystem.getClip(); clip.open(audioIn);
                    if (loop) { bgmClip = clip; bgmClip.loop(Clip.LOOP_CONTINUOUSLY); }
                    clip.start();
                }
            } catch (Exception e) {}
        }
        private void stopBGM() { if (bgmClip != null && bgmClip.isRunning()) { bgmClip.stop(); bgmClip.close(); } }
        private void styleOptionButton(JButton btn, Color color) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 20)); btn.setForeground(Color.WHITE); btn.setBackground(color);
            btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(color.darker()); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(color); }
            });
        }
        void startDBSession() {
            try {
                Connection c = DatabaseHelper.getConnection();
                PreparedStatement s = c.prepareStatement("INSERT INTO quiz_session (id_student, id_quiz, status) VALUES (?,?,'ONGOING')", Statement.RETURN_GENERATED_KEYS);
                s.setInt(1, student.getIdUser()); s.setInt(2, quiz.getIdQuiz()); s.executeUpdate();
                ResultSet rs = s.getGeneratedKeys(); if(rs.next()) sessionId = rs.getInt(1);
            } catch(Exception e){}
        }
        void loadQ(int i) {
            if(i >= quiz.getQuestions().size()) { finish("Quiz Completed!"); return; }
            Question q = quiz.getQuestions().get(i);
            qLabel.setText("<html><div style='text-align: center;'>" + q.getQuestionText() + "</div></html>");
            progressLabel.setText((i+1) + " / " + quiz.getQuestions().size());
            btnA.setText("<html><center>" + q.getOptionA() + "</center></html>"); btnB.setText("<html><center>" + q.getOptionB() + "</center></html>");
            btnC.setText("<html><center>" + q.getOptionC() + "</center></html>"); btnD.setText("<html><center>" + q.getOptionD() + "</center></html>");
            btnA.setEnabled(true); btnB.setEnabled(true); btnC.setEnabled(true); btnD.setEnabled(true);
            startTimer(quiz.getTimeLimit());
        }
        void startTimer(int sec) {
            isRunning = false; if(timerThread!=null) try{timerThread.join();}catch(Exception e){}
            timeLeft = sec; isRunning = true; timerLabel.setForeground(Color.WHITE);
            timerThread = new Thread(() -> {
                while(timeLeft > 0 && isRunning) {
                    try { SwingUtilities.invokeLater(()-> { timerLabel.setText(timeLeft + ""); if(timeLeft <= 5) timerLabel.setForeground(new Color(231, 76, 60)); }); Thread.sleep(1000); timeLeft--; } catch(Exception e){}
                }
                if(timeLeft<=0 && isRunning) SwingUtilities.invokeLater(()-> finish("Time's Up!"));
            }); timerThread.start();
        }

        void process(String ans) {
            isRunning = false;
            // Matikan tombol agar tidak bisa diklik dua kali
            btnA.setEnabled(false); btnB.setEnabled(false); 
            btnC.setEnabled(false); btnD.setEnabled(false);
            
            if (ans != null) {
                Question q = quiz.getQuestions().get(idx); 
                boolean correct = ans.equals(q.getCorrectAnswer());
                
                // --- BAGIAN INI YANG DIUBAH ---
                if (correct) {
                    playAudio("correct.wav", false); // Putar suara BENAR
                } else {
                    playAudio("wrong.wav", false);   // Putar suara SALAH
                }
                // ------------------------------

                try {
                    Connection c = DatabaseHelper.getConnection();
                    PreparedStatement s = c.prepareStatement("INSERT INTO user_answer (id_session,id_question,answer,is_correct) VALUES (?,?,?,?)");
                    s.setInt(1,sessionId); s.setInt(2,q.getIdQuestion()); s.setString(3,ans); s.setBoolean(4,correct); s.executeUpdate();
                } catch(Exception e){}
            }
            Timer delay = new Timer(300, e -> { idx++; loadQ(idx); }); delay.setRepeats(false); delay.start();
        }
        void finish(String statusMsg) {
            isRunning = false; stopBGM();
            try {
                Connection c = DatabaseHelper.getConnection();
                c.prepareStatement("UPDATE quiz_session SET status='COMPLETED', end_time=NOW() WHERE id_session="+sessionId).executeUpdate();
                ResultSet rs = c.prepareStatement("SELECT COUNT(*) FROM user_answer WHERE is_correct=1 AND id_session="+sessionId).executeQuery();
                int totalCorrect = 0; if(rs.next()) totalCorrect = rs.getInt(1); int finalScore = totalCorrect * 10;
                
                PreparedStatement ps = c.prepareStatement("INSERT INTO result (id_session,total_score) VALUES (?,?)");
                ps.setInt(1,sessionId); ps.setInt(2,finalScore); ps.executeUpdate();
                
                PreparedStatement del = c.prepareStatement("DELETE FROM leaderboard WHERE id_quiz=? AND id_student=?");
                del.setInt(1, quiz.getIdQuiz()); del.setInt(2, student.getIdUser()); del.executeUpdate();

                PreparedStatement lb = c.prepareStatement("INSERT INTO leaderboard (id_quiz, id_student, score, `rank`) VALUES (?, ?, ?, 0)");
                lb.setInt(1, quiz.getIdQuiz()); lb.setInt(2, student.getIdUser()); lb.setInt(3, finalScore); lb.executeUpdate();
                
                mainPanel.add(new QuizResultPanel(statusMsg, finalScore, totalCorrect, quiz.getQuestions().size(), quiz.getIdQuiz(), quiz.getTitle()), "RESULT");
                switchCard("RESULT");
            } catch(Exception e){ e.printStackTrace(); JOptionPane.showMessageDialog(this, "Error saving score: " + e.getMessage()); }
        }
    }

    // ==================================================================================
    // 8. VIEW SCORE PANEL (WITH DARK BACKGROUND & WHITE CARD)
    // ==================================================================================
    class ViewScorePanel extends GradientPanel {
        public ViewScorePanel(Student student) {
            setLayout(new BorderLayout()); 
            // 1. Margin Luar (Agar Background Gradient Kelihatan)
            setBorder(new EmptyBorder(30, 40, 30, 40));

            // 2. Kartu Putih
            JPanel contentCard = new JPanel(new BorderLayout()); 
            contentCard.setBackground(Color.WHITE);

            JLabel title = new JLabel("My Quiz History", SwingConstants.CENTER); 
            title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK); title.setBorder(new EmptyBorder(20, 0, 20, 0)); 
            contentCard.add(title, BorderLayout.NORTH);
            
            String[] columns = {"Quiz Title", "Score", "Completed At"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            try {
                Connection conn = DatabaseHelper.getConnection();
                String query = "SELECT q.title, r.total_score, r.completed_at FROM result r JOIN quiz_session qs ON r.id_session = qs.id_session JOIN quiz q ON qs.id_quiz = q.id_quiz WHERE qs.id_student = ? ORDER BY r.completed_at DESC";
                PreparedStatement stmt = conn.prepareStatement(query); stmt.setInt(1, student.getIdUser()); ResultSet rs = stmt.executeQuery();
                while (rs.next()) { model.addRow(new Object[]{rs.getString("title"), rs.getInt("total_score"), rs.getTimestamp("completed_at")}); }
            } catch (Exception e) {}

            JTable table = new JTable(model); styleModernTable(table); 
            JScrollPane scrollPane = new JScrollPane(table); scrollPane.setBorder(null); scrollPane.getViewport().setBackground(Color.WHITE);
            contentCard.add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(Color.WHITE); footer.setBorder(new EmptyBorder(20, 0, 20, 0));
            RoundedGradientButton backBtn = new RoundedGradientButton("Back to Dashboard", COL_DANGER, COL_DANGER.darker()); backBtn.setPreferredSize(new Dimension(250, 45));
            footer.add(backBtn); contentCard.add(footer, BorderLayout.SOUTH);

            add(contentCard, BorderLayout.CENTER); 
            backBtn.addActionListener(e -> switchCard("STUDENT_DASH"));
        }
    }

    // ==================================================================================
    // 9. LEADERBOARD PANEL (WITH DARK BACKGROUND & WHITE CARD)
    // ==================================================================================
    class LeaderboardPanel extends GradientPanel {
        public LeaderboardPanel(int quizId, String quizTitle) {
            setLayout(new BorderLayout()); 
            
            // 1. Margin Luar (Agar Background Gradient Kelihatan)
            setBorder(new EmptyBorder(30, 40, 30, 40));

            // 2. Kartu Putih Container
            JPanel contentCard = new JPanel(new BorderLayout());
            contentCard.setBackground(Color.WHITE);

            JLabel title = new JLabel("Leaderboard: " + quizTitle, SwingConstants.CENTER); 
            title.setFont(FONT_TITLE_BIG); title.setForeground(COL_TEXT_DARK); title.setBorder(new EmptyBorder(20, 0, 20, 0)); 
            contentCard.add(title, BorderLayout.NORTH);

            String[] cols = {"Rank", "Student Name", "Score", "Completed At"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            try {
                Connection conn = DatabaseHelper.getConnection();
                String query = "SELECT u.username, l.score, l.rank, l.completed_at FROM leaderboard l JOIN users u ON l.id_student = u.id_user WHERE l.id_quiz = ? ORDER BY l.score DESC, l.completed_at ASC";
                PreparedStatement stmt = conn.prepareStatement(query); stmt.setInt(1, quizId); ResultSet rs = stmt.executeQuery();
                int manualRank = 1; while(rs.next()) { model.addRow(new Object[]{ "#" + manualRank++, rs.getString("username"), rs.getInt("score"), rs.getTimestamp("completed_at") }); }
            } catch (Exception e) { e.printStackTrace(); }

            JTable table = new JTable(model); 
            styleModernTable(table); // Gunakan style tabel putih
            
            JScrollPane scrollPane = new JScrollPane(table); 
            scrollPane.setBorder(null); scrollPane.getViewport().setBackground(Color.WHITE);
            contentCard.add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(Color.WHITE); footer.setBorder(new EmptyBorder(20, 0, 20, 0));
            String btnText = (currentUser instanceof Teacher) ? "Back to Quiz List" : "Back to Dashboard";
            RoundedGradientButton backBtn = new RoundedGradientButton(btnText, COL_DANGER, COL_DANGER.darker()); 
            backBtn.setPreferredSize(new Dimension(250, 45));
            footer.add(backBtn); contentCard.add(footer, BorderLayout.SOUTH);

            add(contentCard, BorderLayout.CENTER);

            backBtn.addActionListener(e -> {
                if (currentUser instanceof Teacher) { switchCard("VIEW_QUIZ"); } else { switchCard("STUDENT_DASH"); }
            });
        }
    }

    // ==================================================================================
    // 10. EDIT QUIZ PANEL (MODERN STYLE)
    // ==================================================================================
    class EditQuizPanel extends GradientPanel {
        Teacher teacher; int quizId; 
        JTextField titleField = new RoundedTextField(20); JTextField timeField = new RoundedTextField(5);
        JPanel questionsContainer; List<QuestionForm> questionForms = new ArrayList<>();

        public EditQuizPanel(Teacher teacher, int quizId, String currentTitle, int currentTime) {
            this.teacher = teacher; this.quizId = quizId;
            setLayout(new BorderLayout()); 

            JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); header.setOpaque(false);
            JLabel lblTitle = new JLabel("Quiz Title:"); lblTitle.setForeground(Color.WHITE); lblTitle.setFont(FONT_BOLD);
            JLabel lblTime = new JLabel("Time (s):"); lblTime.setForeground(Color.WHITE); lblTime.setFont(FONT_BOLD);
            titleField.setText(currentTitle); titleField.setPreferredSize(new Dimension(300, 40));
            timeField.setText(String.valueOf(currentTime)); timeField.setPreferredSize(new Dimension(80, 40)); timeField.setHorizontalAlignment(JTextField.CENTER);
            header.add(lblTitle); header.add(titleField); header.add(lblTime); header.add(timeField);
            add(header, BorderLayout.NORTH);

            questionsContainer = new JPanel(); questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS)); 
            questionsContainer.setBorder(new EmptyBorder(10, 10, 10, 10)); questionsContainer.setOpaque(false);
            
            loadQuestionsFromDB();
            JScrollPane scrollPane = new JScrollPane(questionsContainer); scrollPane.getVerticalScrollBar().setUnitIncrement(16); scrollPane.setBorder(null); add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setOpaque(false); footer.setBorder(new EmptyBorder(10, 10, 10, 10));
            RoundedGradientButton addBtn = new RoundedGradientButton("Add New Question"); addBtn.setPreferredSize(new Dimension(200, 40));
            RoundedGradientButton saveBtn = new RoundedGradientButton("Save Changes", COL_SUCCESS, COL_SUCCESS.darker()); saveBtn.setPreferredSize(new Dimension(200, 40));
            RoundedGradientButton cancelBtn = new RoundedGradientButton("Cancel", COL_DANGER, COL_DANGER.darker()); cancelBtn.setPreferredSize(new Dimension(150, 40));
            footer.add(addBtn); footer.add(saveBtn); footer.add(cancelBtn); add(footer, BorderLayout.SOUTH);

            cancelBtn.addActionListener(e -> switchCard("VIEW_QUIZ"));
            addBtn.addActionListener(e -> { addQuestionForm(null); SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum())); });
            saveBtn.addActionListener(e -> saveAllChanges());
        }

        private void loadQuestionsFromDB() {
            try {
                Connection conn = DatabaseHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM question WHERE id_quiz = ?"); stmt.setInt(1, quizId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) { addQuestionForm(new Question(rs.getInt("id_question"), rs.getInt("id_quiz"), rs.getString("question_text"), rs.getString("option_a"), rs.getString("option_b"), rs.getString("option_c"), rs.getString("option_d"), rs.getString("correct_answer"))); }
            } catch (Exception e) { e.printStackTrace(); }
        }

        private void addQuestionForm(Question q) {
            QuestionForm form = new QuestionForm(q);
            questionsContainer.add(form); questionsContainer.add(Box.createVerticalStrut(15));
            questionForms.add(form); questionsContainer.revalidate(); questionsContainer.repaint();
        }

        private void saveAllChanges() {
            try {
                Connection conn = DatabaseHelper.getConnection();
                conn.prepareStatement("UPDATE quiz SET title='"+titleField.getText()+"', time_limit="+timeField.getText()+" WHERE id_quiz="+quizId).executeUpdate();
                for (QuestionForm form : questionForms) {
                    if (form.isDeleted) continue;
                    if (form.questionId != 0) {
                        PreparedStatement ps = conn.prepareStatement("UPDATE question SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_answer=? WHERE id_question=?");
                        ps.setString(1, form.qText.getText()); ps.setString(2, form.opA.getText()); ps.setString(3, form.opB.getText()); ps.setString(4, form.opC.getText()); ps.setString(5, form.opD.getText()); ps.setString(6, (String) form.correctBox.getSelectedItem()); ps.setInt(7, form.questionId); ps.executeUpdate();
                    } else {
                        PreparedStatement ps = conn.prepareStatement("INSERT INTO question (id_quiz, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?,?,?,?,?,?,?)");
                        ps.setInt(1, quizId); ps.setString(2, form.qText.getText()); ps.setString(3, form.opA.getText()); ps.setString(4, form.opB.getText()); ps.setString(5, form.opC.getText()); ps.setString(6, form.opD.getText()); ps.setString(7, (String) form.correctBox.getSelectedItem()); ps.executeUpdate();
                    }
                }
                JOptionPane.showMessageDialog(this, "Quiz Updated Successfully!"); mainPanel.add(new ViewQuizPanel(teacher), "VIEW_QUIZ"); switchCard("VIEW_QUIZ");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage()); }
        }

        class QuestionForm extends JPanel {
            int questionId = 0; boolean isDeleted = false;
            JTextField qText = new JTextField(); JTextField opA = new JTextField(); JTextField opB = new JTextField();
            JTextField opC = new JTextField(); JTextField opD = new JTextField(); JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

            public QuestionForm(Question q) {
                setLayout(new BorderLayout()); setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), new EmptyBorder(15, 15, 15, 15)));
                if (q != null) { questionId = q.getIdQuestion(); qText.setText(q.getQuestionText()); opA.setText(q.getOptionA()); opB.setText(q.getOptionB()); opC.setText(q.getOptionC()); opD.setText(q.getOptionD()); correctBox.setSelectedItem(q.getCorrectAnswer()); }

                JPanel formGrid = new JPanel(new GridLayout(3, 2, 15, 15)); formGrid.setBackground(Color.WHITE);
                setupInput(qText, "Question Text"); setupInput(opA, "Option A"); setupInput(opB, "Option B"); setupInput(opC, "Option C"); setupInput(opD, "Option D");
                correctBox.setBackground(Color.WHITE); correctBox.setBorder(BorderFactory.createTitledBorder("Correct Answer"));
                formGrid.add(qText); formGrid.add(correctBox); formGrid.add(opA); formGrid.add(opB); formGrid.add(opC); formGrid.add(opD);
                add(formGrid, BorderLayout.CENTER);

                JButton deleteQBtn = new JButton("Delete Question"); deleteQBtn.setBackground(COL_DANGER); deleteQBtn.setForeground(Color.WHITE); deleteQBtn.setFont(new Font("Segoe UI", Font.BOLD, 12)); deleteQBtn.setBorderPainted(false);
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); btnPanel.setBackground(Color.WHITE); btnPanel.add(deleteQBtn); add(btnPanel, BorderLayout.SOUTH);

                deleteQBtn.addActionListener(e -> {
                    if (JOptionPane.showConfirmDialog(this, "Delete this question?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        isDeleted = true;
                        if (questionId != 0) { try { DatabaseHelper.executeUpdate("DELETE FROM question WHERE id_question=" + questionId); } catch(Exception ex) {} }
                        questionsContainer.remove(this); questionsContainer.revalidate(); questionsContainer.repaint();
                    }
                });
            }
            private void setupInput(JTextField field, String title) {
                field.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
                field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
        }
    }

    //QUIZ RESULT PANEL
    class QuizResultPanel extends GradientPanel {
        public QuizResultPanel(String titleMsg, int score, int correctCount, int totalQuestions, int quizId, String quizTitle) {
            setLayout(new GridBagLayout()); 
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 0, 10, 0); gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel titleLabel = new JLabel(titleMsg); titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            if (titleMsg.equals("Time's Up!")) { titleLabel.setForeground(COL_DANGER); } else { titleLabel.setForeground(new Color(100, 255, 218)); }

            JLabel scoreLabel = new JLabel("Score: " + score); scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48)); scoreLabel.setForeground(Color.WHITE);
            JLabel detailLabel = new JLabel("Correct Answers: " + correctCount + " / " + totalQuestions); detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18)); detailLabel.setForeground(Color.LIGHT_GRAY);

            RoundedGradientButton lbBtn = new RoundedGradientButton("View Leaderboard", COL_SUCCESS, COL_SUCCESS.darker()); lbBtn.setPreferredSize(new Dimension(300, 50));
            RoundedGradientButton homeBtn = new RoundedGradientButton("Back to Dashboard"); homeBtn.setPreferredSize(new Dimension(300, 50));

            gbc.gridy = 0; add(titleLabel, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(20, 0, 10, 0); add(scoreLabel, gbc);
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 40, 0); add(detailLabel, gbc);
            gbc.gridy = 3; gbc.insets = new Insets(10, 0, 10, 0); add(lbBtn, gbc);
            gbc.gridy = 4; add(homeBtn, gbc);

            homeBtn.addActionListener(e -> switchCard("STUDENT_DASH"));
            lbBtn.addActionListener(e -> { mainPanel.add(new LeaderboardPanel(quizId, quizTitle), "LEADERBOARD_STUDENT"); switchCard("LEADERBOARD_STUDENT"); });
        }
    }
}