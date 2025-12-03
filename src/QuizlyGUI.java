import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import java.io.File;

public class QuizlyGUI extends JFrame {
    
    // COLOR PALETTE & FONTS
    final Color ACCENT_BLUE   = new Color(33, 150, 243); 
    final Color DARK_BG       = new Color(30, 30, 30);   
    final Color INPUT_BG      = new Color(60, 60, 60);
    final Color TEXT_WHITE    = new Color(240, 240, 240);
    
    final Color LIGHT_BG      = new Color(236, 240, 241);
    final Color PRIMARY_COLOR = new Color(44, 62, 80);
    final Color DANGER_COLOR  = new Color(231, 76, 60);

    final Font FONT_TITLE_BIG = new Font("Segoe UI", Font.BOLD, 32);
    final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD, 24);
    final Font FONT_NORMAL    = new Font("Segoe UI", Font.PLAIN, 14);
    final Font FONT_BOLD      = new Font("Segoe UI", Font.BOLD, 14);

    // Panel Management
    private JPanel mainPanel;
    private CardLayout cardLayout;
    User currentUser; // Package-private agar bisa diakses oleh LeaderboardPanel

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
        cardLayout.show(mainPanel, "LOGIN");
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

    private void styleButtonDark(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 45)); 
    }

    private void styleTextFieldDark(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_WHITE);
        field.setCaretColor(TEXT_WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 1), 
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private ImageIcon loadImage(String filename, int width, int height) {
        try {
            String path = "assets/" + filename;
            java.io.File f = new java.io.File(path);
            if (!f.exists()) { path = "../assets/" + filename; f = new java.io.File(path); }

            if (f.exists()) {
                ImageIcon icon = new ImageIcon(path);
                Image img = icon.getImage();
                Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(newImg);
            }
        } catch (Exception e) {}
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
            new QuizlyGUI().setVisible(true);
        });
    }

    // 1. LOGIN PANEL
    class LoginPanel extends JPanel {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        public LoginPanel() {
            setLayout(new GridBagLayout());
            setBackground(DARK_BG); 
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(DARK_BG);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 0, 8, 0); 
            gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel imgLabel = new JLabel();
            ImageIcon icon = loadImage("img1.jpg", 120, 120);
            if (icon != null) imgLabel.setIcon(icon);

            JLabel title = new JLabel("Welcome to Quizly");
            title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE);
            
            JLabel uLabel = new JLabel("Username"); uLabel.setFont(FONT_BOLD); uLabel.setForeground(TEXT_WHITE);
            JPanel uLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            uLabelPanel.setBackground(DARK_BG); uLabelPanel.setPreferredSize(new Dimension(350, 20)); uLabelPanel.add(uLabel);

            JLabel pLabel = new JLabel("Password"); pLabel.setFont(FONT_BOLD); pLabel.setForeground(TEXT_WHITE);
            JPanel pLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pLabelPanel.setBackground(DARK_BG); pLabelPanel.setPreferredSize(new Dimension(350, 20)); pLabelPanel.add(pLabel);

            Dimension fieldSize = new Dimension(350, 40);
            userField.setPreferredSize(fieldSize); styleTextFieldDark(userField);
            passField.setPreferredSize(fieldSize); styleTextFieldDark(passField);

            JButton loginBtn = new JButton("Login"); styleButtonDark(loginBtn); loginBtn.setPreferredSize(fieldSize);
            JButton regBtn = new JButton("Register"); styleButtonDark(regBtn); regBtn.setPreferredSize(fieldSize);

            gbc.insets = new Insets(0, 0, 10, 0); formPanel.add(imgLabel, gbc);
            gbc.insets = new Insets(0, 0, 30, 0); formPanel.add(title, gbc);
            gbc.insets = new Insets(5, 0, 5, 0);
            formPanel.add(uLabelPanel, gbc); formPanel.add(userField, gbc);
            formPanel.add(pLabelPanel, gbc); formPanel.add(passField, gbc);
            gbc.insets = new Insets(25, 0, 10, 0); formPanel.add(loginBtn, gbc);
            gbc.insets = new Insets(0, 0, 0, 0); formPanel.add(regBtn, gbc);

            add(formPanel);

            loginBtn.addActionListener(e -> {
                User user = User.login(userField.getText(), new String(passField.getPassword()));
                if (user != null) {
                    setUserSession(user);
                    userField.setText(""); passField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Username/Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            });
            regBtn.addActionListener(e -> switchCard("REGISTER"));
        }
    }

    // 2. REGISTER PANEL
    class RegisterPanel extends JPanel {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STUDENT", "TEACHER"});

        public RegisterPanel() {
            setLayout(new GridBagLayout());
            setBackground(DARK_BG);

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(DARK_BG);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 0, 5, 0);
            gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("Create Account");
            title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE);

            JLabel uLabel = new JLabel("Username"); uLabel.setFont(FONT_BOLD); uLabel.setForeground(TEXT_WHITE);
            JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); p1.setBackground(DARK_BG); p1.setPreferredSize(new Dimension(350,20)); p1.add(uLabel);

            JLabel pLabel = new JLabel("Password"); pLabel.setFont(FONT_BOLD); pLabel.setForeground(TEXT_WHITE);
            JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); p2.setBackground(DARK_BG); p2.setPreferredSize(new Dimension(350,20)); p2.add(pLabel);

            JLabel rLabel = new JLabel("Role"); rLabel.setFont(FONT_BOLD); rLabel.setForeground(TEXT_WHITE);
            JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0)); p3.setBackground(DARK_BG); p3.setPreferredSize(new Dimension(350,20)); p3.add(rLabel);

            Dimension dim = new Dimension(350, 40);
            userField.setPreferredSize(dim); styleTextFieldDark(userField);
            passField.setPreferredSize(dim); styleTextFieldDark(passField);
            
            roleBox.setPreferredSize(dim);
            roleBox.setBackground(INPUT_BG); roleBox.setForeground(Color.BLACK);
            roleBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            JButton regBtn = new JButton("Register"); styleButtonDark(regBtn); regBtn.setPreferredSize(dim);
            JButton backBtn = new JButton("Back"); styleButtonDark(backBtn); backBtn.setPreferredSize(dim); backBtn.setBackground(new Color(80, 80, 80));

            gbc.insets = new Insets(0,0,30,0); formPanel.add(title, gbc);
            gbc.insets = new Insets(5,0,5,0);
            formPanel.add(p1, gbc); formPanel.add(userField, gbc);
            formPanel.add(p2, gbc); formPanel.add(passField, gbc);
            formPanel.add(p3, gbc); formPanel.add(roleBox, gbc);
            gbc.insets = new Insets(25,0,5,0); formPanel.add(regBtn, gbc);
            gbc.insets = new Insets(5,0,5,0); formPanel.add(backBtn, gbc);

            add(formPanel);

            backBtn.addActionListener(e -> switchCard("LOGIN"));
            regBtn.addActionListener(e -> {
                if(User.register(userField.getText(), new String(passField.getPassword()), (String) roleBox.getSelectedItem())) {
                    JOptionPane.showMessageDialog(this, "Registration Successful! Please Login.");
                    switchCard("LOGIN");
                } else {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    // 3. TEACHER DASHBOARD
    class TeacherDashboard extends JPanel {
        public TeacherDashboard(Teacher teacher) {
            setLayout(new GridBagLayout());
            setBackground(DARK_BG);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 0, 15, 0); gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("Teacher Dashboard");
            title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE);

            JLabel subtitle = new JLabel("Welcome, " + teacher.getUsername());
            subtitle.setFont(FONT_NORMAL); subtitle.setForeground(TEXT_WHITE);

            Dimension btnSize = new Dimension(350, 55);
            JButton createBtn = new JButton("Create New Quiz");
            styleButtonDark(createBtn); createBtn.setBackground(ACCENT_BLUE); createBtn.setPreferredSize(btnSize);
            
            JButton viewBtn = new JButton("My Quizzes & Leaderboard");
            styleButtonDark(viewBtn); viewBtn.setBackground(new Color(46, 204, 113)); viewBtn.setPreferredSize(btnSize);

            JButton logoutBtn = new JButton("Logout");
            styleButtonDark(logoutBtn); logoutBtn.setBackground(DANGER_COLOR); logoutBtn.setPreferredSize(new Dimension(350, 45));

            gbc.gridy = 0; add(title, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0); add(subtitle, gbc);
            gbc.gridy = 2; gbc.insets = new Insets(10, 0, 10, 0); add(createBtn, gbc);
            gbc.gridy = 3; add(viewBtn, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(40, 0, 0, 0); add(logoutBtn, gbc);

            logoutBtn.addActionListener(e -> switchCard("LOGIN"));
            createBtn.addActionListener(e -> { mainPanel.add(new CreateQuizPanel(teacher), "CREATE"); switchCard("CREATE"); });
            viewBtn.addActionListener(e -> { mainPanel.add(new ViewQuizPanel(teacher), "VIEW_QUIZ"); switchCard("VIEW_QUIZ"); });
        }
    }

    // 4. STUDENT DASHBOARD
    class StudentDashboard extends JPanel {
        public StudentDashboard(Student student) {
            setLayout(new GridBagLayout());
            setBackground(DARK_BG);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("Student Dashboard");
            title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE);

            JLabel subtitle = new JLabel("Logged in as: " + student.getUsername());
            subtitle.setFont(FONT_NORMAL); subtitle.setForeground(TEXT_WHITE);

            JTextField keyField = new JTextField();
            keyField.setPreferredSize(new Dimension(350, 45));
            keyField.setHorizontalAlignment(JTextField.CENTER);
            styleTextFieldDark(keyField);
            keyField.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(ACCENT_BLUE), "Enter Quiz Key", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new Font("Segoe UI", Font.PLAIN, 12), Color.GRAY));

            JButton joinBtn = new JButton("Start Quiz");
            styleButtonDark(joinBtn); joinBtn.setBackground(ACCENT_BLUE); joinBtn.setPreferredSize(new Dimension(350, 45));

            JButton historyBtn = new JButton("View My Grades");
            styleButtonDark(historyBtn); historyBtn.setBackground(new Color(230, 126, 34)); historyBtn.setPreferredSize(new Dimension(350, 45));

            JButton logoutBtn = new JButton("Logout");
            styleButtonDark(logoutBtn); logoutBtn.setBackground(DANGER_COLOR); logoutBtn.setPreferredSize(new Dimension(350, 45));

            gbc.gridy = 0; gbc.insets = new Insets(10, 0, 5, 0); add(title, gbc);
            gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0); add(subtitle, gbc);
            gbc.gridy = 2; gbc.insets = new Insets(10, 0, 10, 0); add(keyField, gbc);
            gbc.gridy = 3; add(joinBtn, gbc);
            gbc.gridy = 4; gbc.insets = new Insets(30, 0, 10, 0); JSeparator sep = new JSeparator(); sep.setPreferredSize(new Dimension(200, 1)); sep.setForeground(Color.GRAY); add(sep, gbc);
            gbc.gridy = 5; gbc.insets = new Insets(10, 0, 10, 0); add(historyBtn, gbc);
            gbc.gridy = 6; gbc.insets = new Insets(30, 0, 0, 0); add(logoutBtn, gbc);

            logoutBtn.addActionListener(e -> switchCard("LOGIN"));
            historyBtn.addActionListener(e -> { mainPanel.add(new ViewScorePanel(student), "SCORE"); switchCard("SCORE"); });
            
            joinBtn.addActionListener(e -> {
                String key = keyField.getText().trim().toUpperCase();
                Quiz quiz = student.joinQuiz(key);
                if (quiz != null) { mainPanel.add(new DoQuizPanel(student, quiz), "DO"); switchCard("DO"); } 
                else { JOptionPane.showMessageDialog(this, "Quiz not found or inactive!", "Error", JOptionPane.ERROR_MESSAGE); }
            });
        }
    }

    // 5. CREATE QUIZ PANEL
    class CreateQuizPanel extends JPanel {
        List<Question> questions = new ArrayList<>();
        JTextField titleField = new JTextField(20);
        JTextField timeField = new JTextField(5);
        JTextField qText = new JTextField();
        JTextField opA = new JTextField(); JTextField opB = new JTextField(); JTextField opC = new JTextField(); JTextField opD = new JTextField();
        JComboBox<String> correctBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        public CreateQuizPanel(Teacher teacher) {
            setLayout(new BorderLayout());
            setBackground(LIGHT_BG);

            JPanel header = new JPanel(new GridLayout(1, 2, 20, 0));
            header.setBackground(Color.WHITE); header.setBorder(new EmptyBorder(15, 20, 15, 20));
            JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(Color.WHITE); p1.add(new JLabel("Quiz Title: "), BorderLayout.WEST); p1.add(titleField, BorderLayout.CENTER);
            JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(Color.WHITE); p2.add(new JLabel("  Time (sec): "), BorderLayout.WEST); p2.add(timeField, BorderLayout.CENTER);
            header.add(p1); header.add(p2); add(header, BorderLayout.NORTH);

            JPanel form = new JPanel(new GridLayout(8, 1, 5, 5));
            form.setBackground(LIGHT_BG); form.setBorder(new EmptyBorder(10, 50, 10, 50));
            qText.setBorder(BorderFactory.createTitledBorder("Question Text"));
            opA.setBorder(BorderFactory.createTitledBorder("Option A"));
            opB.setBorder(BorderFactory.createTitledBorder("Option B"));
            opC.setBorder(BorderFactory.createTitledBorder("Option C"));
            opD.setBorder(BorderFactory.createTitledBorder("Option D"));
            correctBox.setBorder(BorderFactory.createTitledBorder("Correct Answer")); correctBox.setBackground(Color.WHITE);

            form.add(qText); form.add(opA); form.add(opB); form.add(opC); form.add(opD); form.add(correctBox);
            JButton addQBtn = new JButton("Save Question to Draft"); styleButtonDark(addQBtn); addQBtn.setBackground(ACCENT_BLUE); form.add(addQBtn);
            add(new JScrollPane(form), BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(PRIMARY_COLOR); footer.setBorder(new EmptyBorder(10, 10, 10, 10));
            JLabel countLabel = new JLabel("Questions in Draft: 0   |   "); countLabel.setForeground(Color.WHITE); countLabel.setFont(FONT_BOLD);
            JButton finishBtn = new JButton("Finish & Upload Quiz"); styleButtonDark(finishBtn); finishBtn.setBackground(new Color(39, 174, 96)); finishBtn.setPreferredSize(new Dimension(200, 40));
            JButton backBtn = new JButton("Cancel / Back"); styleButtonDark(backBtn); backBtn.setBackground(DANGER_COLOR); backBtn.setPreferredSize(new Dimension(150, 40));

            footer.add(countLabel); footer.add(finishBtn); footer.add(backBtn); add(footer, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> {
                if (!questions.isEmpty() || !qText.getText().isEmpty()) {
                    int confirm = JOptionPane.showConfirmDialog(this, "You have unsaved questions. Go back?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }
                switchCard("TEACHER_DASH");
            });

            addQBtn.addActionListener(e -> {
                if(qText.getText().trim().isEmpty() || opA.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Fields empty!"); return; }
                questions.add(new Question(qText.getText(), opA.getText(), opB.getText(), opC.getText(), opD.getText(), (String)correctBox.getSelectedItem()));
                countLabel.setText("Questions in Draft: " + questions.size() + "   |   ");
                JOptionPane.showMessageDialog(this, "Question Saved to Draft!");
                qText.setText(""); opA.setText(""); opB.setText(""); opC.setText(""); opD.setText(""); qText.requestFocus();
            });

            finishBtn.addActionListener(e -> {
                if(questions.isEmpty()) { JOptionPane.showMessageDialog(this, "Add questions first!"); return; }
                try {
                    int time = Integer.parseInt(timeField.getText());
                    Quiz q = teacher.createQuiz(titleField.getText(), time, questions);
                    if(q != null) { JOptionPane.showMessageDialog(this, "Quiz Created! Key: " + q.getQuizKey()); switchCard("TEACHER_DASH"); }
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Time!"); }
            });
        }
    }

    // 6. VIEW QUIZ PANEL (TEACHER - CRUD)
    class ViewQuizPanel extends JPanel {
        JTable table; DefaultTableModel model; Teacher teacher;

        public ViewQuizPanel(Teacher teacher) {
            this.teacher = teacher;
            setLayout(new BorderLayout()); setBackground(DARK_BG);

            JLabel title = new JLabel("Manage My Quizzes", SwingConstants.CENTER);
            title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE); title.setBorder(new EmptyBorder(30, 0, 20, 0));
            add(title, BorderLayout.NORTH);

            String[] cols = {"ID", "Title", "Key", "Time (s)", "Status", "Actions"};
            model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return column == 5; } };

            table = new JTable(model);
            table.setRowHeight(55); table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            table.setBackground(INPUT_BG); table.setForeground(TEXT_WHITE); table.setGridColor(new Color(80, 80, 80));
            table.setShowVerticalLines(false); table.setSelectionBackground(ACCENT_BLUE); table.setSelectionForeground(Color.WHITE);

            table.getTableHeader().setDefaultRenderer((t, value, isSelected, hasFocus, row, col) -> {
                JLabel l = new JLabel(value.toString()); l.setOpaque(true); l.setBackground(ACCENT_BLUE); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setHorizontalAlignment(JLabel.CENTER); return l;
            });
            table.getTableHeader().setPreferredSize(new Dimension(0, 50));

            javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment(JLabel.CENTER); centerRenderer.setBackground(INPUT_BG); centerRenderer.setForeground(TEXT_WHITE);
            for(int i=0; i<5; i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

            table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
            table.getColumnModel().getColumn(5).setPreferredWidth(280);

            refreshTableData();

            JScrollPane scrollPane = new JScrollPane(table); scrollPane.getViewport().setBackground(DARK_BG); scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BG));
            JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setBackground(DARK_BG); tableContainer.setBorder(new EmptyBorder(0, 50, 0, 50));
            tableContainer.add(scrollPane, BorderLayout.CENTER); add(tableContainer, BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); footer.setBackground(DARK_BG);
            JButton openBtn = new JButton("Open Leaderboard"); styleButtonDark(openBtn); openBtn.setBackground(new Color(39, 174, 96)); openBtn.setPreferredSize(new Dimension(200, 45));
            JButton backBtn = new JButton("Back to Dashboard"); styleButtonDark(backBtn); backBtn.setBackground(DANGER_COLOR); backBtn.setPreferredSize(new Dimension(200, 45));
            footer.add(openBtn); footer.add(backBtn); add(footer, BorderLayout.SOUTH);

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
                panel.setBackground(isSelected ? table.getSelectionBackground() : INPUT_BG); panel.setBorder(new EmptyBorder(2, 5, 2, 5)); 
                JButton statusBtn = new JButton("On/Off"); styleMiniButton(statusBtn, new Color(142, 68, 173));
                JButton editBtn = new JButton("Edit"); styleMiniButton(editBtn, new Color(243, 156, 18));
                JButton delBtn = new JButton("Del"); styleMiniButton(delBtn, DANGER_COLOR);
                panel.add(statusBtn); panel.add(editBtn); panel.add(delBtn);
                return panel;
            }
        }

        class ButtonEditor extends DefaultCellEditor {
            protected JPanel panel; protected JButton statusBtn, editBtn, delBtn; private int currentRow;
            public ButtonEditor(JCheckBox checkBox) {
                super(checkBox);
                panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); panel.setBackground(INPUT_BG); panel.setBorder(new EmptyBorder(2, 5, 2, 5));
                statusBtn = new JButton("On/Off"); styleMiniButton(statusBtn, new Color(142, 68, 173));
                editBtn = new JButton("Edit"); styleMiniButton(editBtn, new Color(243, 156, 18));
                delBtn = new JButton("Del"); styleMiniButton(delBtn, DANGER_COLOR);
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
            btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(75, 35));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    // 7. DO QUIZ PANEL (AUDIO + GAMIFIED)
    class DoQuizPanel extends JPanel {
        Student student; Quiz quiz; int idx = 0; int sessionId = 0;
        JLabel qLabel = new JLabel(); JLabel timerLabel = new JLabel("0s"); JLabel progressLabel = new JLabel();
        JButton btnA = new JButton(); JButton btnB = new JButton(); JButton btnC = new JButton(); JButton btnD = new JButton();
        final Color COL_A = new Color(231, 76, 60); final Color COL_B = new Color(41, 128, 185);
        final Color COL_C = new Color(241, 196, 15); final Color COL_D = new Color(39, 174, 96);
        Thread timerThread; boolean isRunning = false; int timeLeft;
        Clip bgmClip;

        public DoQuizPanel(Student student, Quiz quiz) {
            this.student = student; this.quiz = quiz;
            setLayout(new BorderLayout()); setBackground(DARK_BG);

            JPanel topPanel = new JPanel(new BorderLayout()); topPanel.setBackground(DARK_BG); topPanel.setBorder(new EmptyBorder(20, 30, 0, 30));
            progressLabel.setFont(FONT_BOLD); progressLabel.setForeground(TEXT_WHITE);
            timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 30)); timerLabel.setForeground(Color.WHITE);
            topPanel.add(progressLabel, BorderLayout.WEST); topPanel.add(timerLabel, BorderLayout.EAST); add(topPanel, BorderLayout.NORTH);

            JPanel qPanel = new JPanel(new BorderLayout()); qPanel.setBackground(DARK_BG); qPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
            qLabel.setHorizontalAlignment(SwingConstants.CENTER); qLabel.setFont(new Font("Segoe UI", Font.BOLD, 28)); qLabel.setForeground(TEXT_WHITE);
            qPanel.add(qLabel, BorderLayout.CENTER); add(qPanel, BorderLayout.CENTER);

            JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15)); gridPanel.setBackground(DARK_BG); gridPanel.setBorder(new EmptyBorder(20, 20, 30, 20)); gridPanel.setPreferredSize(new Dimension(0, 300));
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
            });
            timerThread.start();
        }

        void process(String ans) {
            isRunning = false;
            btnA.setEnabled(false); btnB.setEnabled(false); btnC.setEnabled(false); btnD.setEnabled(false);
            if (ans != null) {
                Question q = quiz.getQuestions().get(idx); boolean correct = ans.equals(q.getCorrectAnswer());
                if (!correct) playAudio("wrong.wav", false);
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

    // 8. VIEW SCORE PANEL (STUDENT HISTORY)
    class ViewScorePanel extends JPanel {
        public ViewScorePanel(Student student) {
            setLayout(new BorderLayout()); setBackground(DARK_BG);
            JLabel title = new JLabel("My Quiz History", SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE); title.setBorder(new EmptyBorder(30, 0, 30, 0)); add(title, BorderLayout.NORTH);
            String[] columns = {"Quiz Title", "Score", "Completed At"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            try {
                Connection conn = DatabaseHelper.getConnection();
                String query = "SELECT q.title, r.total_score, r.completed_at FROM result r JOIN quiz_session qs ON r.id_session = qs.id_session JOIN quiz q ON qs.id_quiz = q.id_quiz WHERE qs.id_student = ? ORDER BY r.completed_at DESC";
                PreparedStatement stmt = conn.prepareStatement(query); stmt.setInt(1, student.getIdUser()); ResultSet rs = stmt.executeQuery();
                while (rs.next()) { model.addRow(new Object[]{rs.getString("title"), rs.getInt("total_score"), rs.getTimestamp("completed_at")}); }
            } catch (Exception e) {}

            JTable table = new JTable(model); table.setRowHeight(40); table.setFont(new Font("Segoe UI", Font.PLAIN, 16)); table.setBackground(INPUT_BG); table.setForeground(TEXT_WHITE); table.setGridColor(new Color(80, 80, 80)); table.setShowVerticalLines(false); table.setSelectionBackground(ACCENT_BLUE); table.setSelectionForeground(Color.WHITE);
            table.getTableHeader().setDefaultRenderer((t, value, isSelected, hasFocus, row, col) -> { JLabel l = new JLabel(value.toString()); l.setOpaque(true); l.setBackground(ACCENT_BLUE); l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setHorizontalAlignment(JLabel.CENTER); return l; });
            table.getTableHeader().setPreferredSize(new Dimension(0, 50));
            javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER); centerRenderer.setBackground(INPUT_BG); centerRenderer.setForeground(TEXT_WHITE);
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); 

            JScrollPane scrollPane = new JScrollPane(table); scrollPane.getViewport().setBackground(DARK_BG); scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BG));
            JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setBackground(DARK_BG); tableContainer.setBorder(new EmptyBorder(0, 50, 0, 50));
            tableContainer.add(scrollPane, BorderLayout.CENTER); add(tableContainer, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(DARK_BG); footer.setBorder(new EmptyBorder(30, 0, 30, 0));
            JButton backBtn = new JButton("Back to Dashboard"); styleButtonDark(backBtn); backBtn.setBackground(DANGER_COLOR); backBtn.setPreferredSize(new Dimension(250, 50));
            footer.add(backBtn); add(footer, BorderLayout.SOUTH);
            backBtn.addActionListener(e -> switchCard("STUDENT_DASH"));
        }
    }

    // 9. LEADERBOARD PANEL
    class LeaderboardPanel extends JPanel {
        public LeaderboardPanel(int quizId, String quizTitle) {
            setLayout(new BorderLayout()); setBackground(DARK_BG);
            JLabel title = new JLabel("Leaderboard: " + quizTitle, SwingConstants.CENTER); title.setFont(FONT_TITLE_BIG); title.setForeground(ACCENT_BLUE); title.setBorder(new EmptyBorder(30, 0, 30, 0)); add(title, BorderLayout.NORTH);
            String[] cols = {"Rank", "Student Name", "Score", "Completed At"};
            DefaultTableModel model = new DefaultTableModel(cols, 0);
            try {
                Connection conn = DatabaseHelper.getConnection();
                String query = "SELECT u.username, l.score, l.rank, l.completed_at FROM leaderboard l JOIN users u ON l.id_student = u.id_user WHERE l.id_quiz = ? ORDER BY l.score DESC, l.completed_at ASC";
                PreparedStatement stmt = conn.prepareStatement(query); stmt.setInt(1, quizId); ResultSet rs = stmt.executeQuery();
                int manualRank = 1; while(rs.next()) { model.addRow(new Object[]{ "#" + manualRank++, rs.getString("username"), rs.getInt("score"), rs.getTimestamp("completed_at") }); }
            } catch (Exception e) { e.printStackTrace(); }

            JTable table = new JTable(model); table.setRowHeight(40); table.setFont(new Font("Segoe UI", Font.PLAIN, 16)); table.setBackground(INPUT_BG); table.setForeground(TEXT_WHITE); table.setGridColor(new Color(80, 80, 80)); table.setShowVerticalLines(false);
            table.getTableHeader().setDefaultRenderer((t, value, isSelected, hasFocus, row, col) -> { JLabel l = new JLabel(value.toString()); l.setOpaque(true); l.setBackground(ACCENT_BLUE); l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 16)); l.setHorizontalAlignment(JLabel.CENTER); return l; });
            table.getTableHeader().setPreferredSize(new Dimension(0, 50));
            javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer(); centerRenderer.setHorizontalAlignment(JLabel.CENTER); centerRenderer.setBackground(INPUT_BG); centerRenderer.setForeground(TEXT_WHITE);
            table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

            JScrollPane scrollPane = new JScrollPane(table); scrollPane.getViewport().setBackground(DARK_BG); scrollPane.setBorder(BorderFactory.createLineBorder(INPUT_BG));
            JPanel tableContainer = new JPanel(new BorderLayout()); tableContainer.setBackground(DARK_BG); tableContainer.setBorder(new EmptyBorder(0, 50, 0, 50));
            tableContainer.add(scrollPane, BorderLayout.CENTER); add(tableContainer, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(DARK_BG); footer.setBorder(new EmptyBorder(30, 0, 30, 0));
            String btnText = (currentUser instanceof Teacher) ? "Back to Quiz List" : "Back to Dashboard";
            JButton backBtn = new JButton(btnText); styleButtonDark(backBtn); backBtn.setBackground(DANGER_COLOR); backBtn.setPreferredSize(new Dimension(250, 50));
            footer.add(backBtn); add(footer, BorderLayout.SOUTH);

            backBtn.addActionListener(e -> {
                if (currentUser instanceof Teacher) { switchCard("VIEW_QUIZ"); } else { switchCard("STUDENT_DASH"); }
            });
        }
    }

    // 10. EDIT QUIZ PANEL
    class EditQuizPanel extends JPanel {
        Teacher teacher; int quizId; JTextField titleField = new JTextField(); JTextField timeField = new JTextField();
        JPanel questionsContainer; List<QuestionForm> questionForms = new ArrayList<>();

        public EditQuizPanel(Teacher teacher, int quizId, String currentTitle, int currentTime) {
            this.teacher = teacher; this.quizId = quizId;
            setLayout(new BorderLayout()); setBackground(LIGHT_BG);

            JPanel header = new JPanel(new GridLayout(1, 2, 20, 0)); header.setBackground(Color.WHITE); header.setBorder(new EmptyBorder(15, 20, 15, 20));
            setupHeaderField(titleField, currentTitle); setupHeaderField(timeField, String.valueOf(currentTime));
            JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(Color.WHITE); p1.add(new JLabel("Quiz Title: "), BorderLayout.WEST); p1.add(titleField, BorderLayout.CENTER);
            JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(Color.WHITE); p2.add(new JLabel("  Time (sec): "), BorderLayout.WEST); p2.add(timeField, BorderLayout.CENTER);
            header.add(p1); header.add(p2); add(header, BorderLayout.NORTH);

            questionsContainer = new JPanel(); questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS)); questionsContainer.setBackground(LIGHT_BG); questionsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
            loadQuestionsFromDB();
            JScrollPane scrollPane = new JScrollPane(questionsContainer); scrollPane.getVerticalScrollBar().setUnitIncrement(16); scrollPane.setBorder(null); add(scrollPane, BorderLayout.CENTER);

            JPanel footer = new JPanel(); footer.setBackground(PRIMARY_COLOR); footer.setBorder(new EmptyBorder(10, 10, 10, 10));
            JButton addBtn = new JButton("Add New Question"); styleButtonDark(addBtn); addBtn.setBackground(ACCENT_BLUE); addBtn.setPreferredSize(new Dimension(200, 40));
            JButton saveBtn = new JButton("Save Changes"); styleButtonDark(saveBtn); saveBtn.setBackground(new Color(39, 174, 96)); saveBtn.setPreferredSize(new Dimension(200, 40));
            JButton cancelBtn = new JButton("Cancel"); styleButtonDark(cancelBtn); cancelBtn.setBackground(DANGER_COLOR); cancelBtn.setPreferredSize(new Dimension(150, 40));
            footer.add(addBtn); footer.add(saveBtn); footer.add(cancelBtn); add(footer, BorderLayout.SOUTH);

            cancelBtn.addActionListener(e -> switchCard("VIEW_QUIZ"));
            addBtn.addActionListener(e -> { addQuestionForm(null); SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum())); });
            saveBtn.addActionListener(e -> saveAllChanges());
        }

        private void setupHeaderField(JTextField field, String text) {
            field.setText(text); field.setFont(new Font("Segoe UI", Font.PLAIN, 16)); field.setForeground(Color.BLACK); field.setBackground(Color.WHITE); field.setCaretColor(Color.BLACK);
            field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
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
                JOptionPane.showMessageDialog(this, "Quiz Updated Successfully!");
                mainPanel.add(new ViewQuizPanel(teacher), "VIEW_QUIZ"); switchCard("VIEW_QUIZ");
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

                JButton deleteQBtn = new JButton("Delete Question"); deleteQBtn.setBackground(DANGER_COLOR); deleteQBtn.setForeground(Color.WHITE); deleteQBtn.setFont(new Font("Segoe UI", Font.BOLD, 12)); deleteQBtn.setBorderPainted(false);
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
                field.setFont(new Font("Segoe UI", Font.PLAIN, 14)); field.setForeground(Color.BLACK); field.setBackground(new Color(250, 250, 250)); field.setCaretColor(Color.BLACK);
                field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), title, javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
        }
    }

    //QUIZ RESULT PANEL
    class QuizResultPanel extends JPanel {
        public QuizResultPanel(String titleMsg, int score, int correctCount, int totalQuestions, int quizId, String quizTitle) {
            setLayout(new GridBagLayout()); setBackground(DARK_BG);
            GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 0, 10, 0); gbc.gridx = 0; gbc.anchor = GridBagConstraints.CENTER;

            JLabel titleLabel = new JLabel(titleMsg); titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
            if (titleMsg.equals("Time's Up!")) { titleLabel.setForeground(DANGER_COLOR); } else { titleLabel.setForeground(ACCENT_BLUE); }

            JLabel scoreLabel = new JLabel("Score: " + score); scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 48)); scoreLabel.setForeground(Color.WHITE);
            JLabel detailLabel = new JLabel("Correct Answers: " + correctCount + " / " + totalQuestions); detailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18)); detailLabel.setForeground(Color.LIGHT_GRAY);

            JButton lbBtn = new JButton("View Leaderboard"); styleButtonDark(lbBtn); lbBtn.setBackground(new Color(39, 174, 96)); lbBtn.setPreferredSize(new Dimension(300, 50));
            JButton homeBtn = new JButton("Back to Dashboard"); styleButtonDark(homeBtn); homeBtn.setBackground(PRIMARY_COLOR); homeBtn.setPreferredSize(new Dimension(300, 50));

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