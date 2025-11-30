import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class QuizSession implements Runnable {
    private int idSession;
    private Student student;
    private Quiz quiz;
    private Map<Integer, String> userAnswers;
    private int currentQuestionIndex;
    private boolean timeUp;
    private Thread timerThread;
    private int timeLimit;
    
    // Constructor
    public QuizSession(Student student, Quiz quiz) {
        this.student = student;
        this.quiz = quiz;
        this.userAnswers = new HashMap<>();
        this.currentQuestionIndex = 0;
        this.timeUp = false;
        this.timeLimit = quiz.getTimeLimit();
    }
    
    // Getters and Setters
    public int getIdSession() {
        return idSession;
    }
    
    public void setIdSession(int idSession) {
        this.idSession = idSession;
    }
    
    // Method untuk memulai quiz session
    public void startQuiz() {
        try {
            // Create quiz session di database
            Connection conn = DatabaseHelper.getConnection();
            String query = "INSERT INTO quiz_session (id_student, id_quiz, status) VALUES (?, ?, 'ONGOING')";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, student.getIdUser());
            stmt.setInt(2, quiz.getIdQuiz());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.idSession = rs.getInt(1);
                }
            }
            
            System.out.println("\n========================================");
            System.out.println("QUIZ STARTED: " + quiz.getTitle());
            System.out.println("Total Questions: " + quiz.getQuestions().size());
            System.out.println("Time Limit per Question: " + timeLimit + " seconds");
            System.out.println("========================================\n");
            
            // Mulai mengerjakan soal
            processQuestions();
            
        } catch (SQLException e) {
            System.out.println("Failed to start quiz session!");
            e.printStackTrace();
        }
    }
    
    // Method untuk process semua questions
    private void processQuestions() {
        Scanner scanner = new Scanner(System.in);
        
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            currentQuestionIndex = i;
            Question question = quiz.getQuestions().get(i);
            
            // Display question
            question.displayQuestion(i + 1);
            
            // Start timer thread
            timeUp = false;
            timerThread = new Thread(this);
            timerThread.start();
            
            // Get user answer
            System.out.print("Your answer (A/B/C/D): ");
            String answer = null;
            
            // Wait for answer or timeout
            while (!timeUp && answer == null) {
                if (scanner.hasNextLine()) {
                    answer = scanner.nextLine().trim().toUpperCase();
                    break;
                }
            }
            
            // Stop timer
            timerThread.interrupt();
            
            if (timeUp) {
                System.out.println("⏰ TIME UP! Moving to next question...");
                answer = null; // No answer
            }
            
            // Save answer
            userAnswers.put(question.getIdQuestion(), answer);
            saveUserAnswer(question.getIdQuestion(), answer);
            
            System.out.println();
        }
        
        // Finish quiz
        finishQuiz();
    }
    
    // Method untuk save user answer ke database
    private void saveUserAnswer(int questionId, String answer) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Get correct answer
            String getCorrectQuery = "SELECT correct_answer FROM question WHERE id_question = ?";
            PreparedStatement getStmt = conn.prepareStatement(getCorrectQuery);
            getStmt.setInt(1, questionId);
            ResultSet rs = getStmt.executeQuery();
            
            String correctAnswer = "";
            if (rs.next()) {
                correctAnswer = rs.getString("correct_answer");
            }
            
            boolean isCorrect = (answer != null && answer.equalsIgnoreCase(correctAnswer));
            
            // Save to database
            String query = "INSERT INTO user_answer (id_session, id_question, answer, is_correct) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idSession);
            stmt.setInt(2, questionId);
            stmt.setString(3, answer);
            stmt.setBoolean(4, isCorrect);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Failed to save answer!");
            e.printStackTrace();
        }
    }
    
    // Method untuk finish quiz
    private void finishQuiz() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Update quiz session status
            String updateSessionQuery = "UPDATE quiz_session SET status = 'COMPLETED', end_time = NOW() WHERE id_session = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSessionQuery);
            updateStmt.setInt(1, idSession);
            updateStmt.executeUpdate();
            
            // Calculate score
            int totalScore = calculateScore();
            
            // Save result
            Result result = new Result(idSession, totalScore);
            result.saveToDatabase();
            
            // Update leaderboard
            updateLeaderboard(totalScore);
            
            // Display result
            displayResult(totalScore);
            
        } catch (SQLException e) {
            System.out.println("Failed to finish quiz!");
            e.printStackTrace();
        }
    }
    
    // Method untuk calculate score
    private int calculateScore() {
        int correctAnswers = 0;
        
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT COUNT(*) as correct FROM user_answer WHERE id_session = ? AND is_correct = TRUE";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idSession);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                correctAnswers = rs.getInt("correct");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Score: correct answers * 10
        return correctAnswers * 10;
    }
    
    // Method untuk update leaderboard
    private void updateLeaderboard(int score) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Insert ke leaderboard
            String insertQuery = "INSERT INTO leaderboard (id_quiz, id_student, score, rank) VALUES (?, ?, ?, 0)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, quiz.getIdQuiz());
            insertStmt.setInt(2, student.getIdUser());
            insertStmt.setInt(3, score);
            insertStmt.executeUpdate();
            
            // Update ranks
            String selectQuery = "SELECT id_leaderboard FROM leaderboard WHERE id_quiz = ? ORDER BY score DESC, completed_at ASC";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, quiz.getIdQuiz());
            ResultSet rs = selectStmt.executeQuery();
            
            int rank = 1;
            while (rs.next()) {
                int leaderboardId = rs.getInt("id_leaderboard");
                String updateQuery = "UPDATE leaderboard SET rank = ? WHERE id_leaderboard = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, rank++);
                updateStmt.setInt(2, leaderboardId);
                updateStmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to update leaderboard!");
            e.printStackTrace();
        }
    }
    
    // Method untuk display result
    private void displayResult(int totalScore) {
        int totalQuestions = quiz.getQuestions().size();
        int maxScore = totalQuestions * 10;
        int correctAnswers = totalScore / 10;
        
        System.out.println("\n========================================");
        System.out.println("QUIZ COMPLETED!");
        System.out.println("========================================");
        System.out.println("Your Score: " + totalScore + " / " + maxScore);
        System.out.println("Correct Answers: " + correctAnswers + " / " + totalQuestions);
        System.out.println("Percentage: " + String.format("%.2f", (totalScore * 100.0 / maxScore)) + "%");
        System.out.println("========================================\n");
    }
    
    // Timer thread implementation (Interface Runnable)
    @Override
    public void run() {
        try {
            Thread.sleep(timeLimit * 1000); // Convert to milliseconds
            timeUp = true;
        } catch (InterruptedException e) {
            // Timer stopped by user input
        }
    }
}