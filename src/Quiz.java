import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Quiz {
    private int idQuiz;
    private String title;
    private String quizKey;
    private int timeLimit;
    private String status;
    private int idTeacher;
    private List<Question> questions;
    
    // Constructor
    public Quiz() {
        this.questions = new ArrayList<>();
    }
    
    public Quiz(int idQuiz, String title, String quizKey, int timeLimit, String status, int idTeacher) {
        this.idQuiz = idQuiz;
        this.title = title;
        this.quizKey = quizKey;
        this.timeLimit = timeLimit;
        this.status = status;
        this.idTeacher = idTeacher;
        this.questions = new ArrayList<>();
    }
    
    // Getters and Setters (Encapsulation)
    public int getIdQuiz() {
        return idQuiz;
    }
    
    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getQuizKey() {
        return quizKey;
    }
    
    public void setQuizKey(String quizKey) {
        this.quizKey = quizKey;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getIdTeacher() {
        return idTeacher;
    }
    
    public void setIdTeacher(int idTeacher) {
        this.idTeacher = idTeacher;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    // Method untuk menambahkan question ke quiz
    public void addQuestion(Question question) {
        this.questions.add(question);
    }
    
    // Method untuk load questions dari database
    public void loadQuestionsFromDatabase() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT * FROM question WHERE id_quiz = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, this.idQuiz);
            
            ResultSet rs = stmt.executeQuery();
            
            this.questions.clear();
            while (rs.next()) {
                Question question = new Question(
                    rs.getInt("id_question"),
                    rs.getInt("id_quiz"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_answer")
                );
                this.questions.add(question);
            }
            
            System.out.println("Loaded " + this.questions.size() + " questions.");
            
        } catch (SQLException e) {
            System.out.println("Failed to load questions!");
            e.printStackTrace();
        }
    }
    
    // Method untuk menampilkan detail quiz
    public void displayQuizInfo() {
        System.out.println("\n===== QUIZ INFO =====");
        System.out.println("Title: " + title);
        System.out.println("Quiz Key: " + quizKey);
        System.out.println("Time Limit: " + timeLimit + " seconds per question");
        System.out.println("Total Questions: " + questions.size());
        System.out.println("Status: " + status);
        System.out.println("=====================\n");
    }
    
    // Method untuk update status quiz
    public boolean updateStatus(String newStatus) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "UPDATE quiz SET status = ? WHERE id_quiz = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, newStatus);
            stmt.setInt(2, this.idQuiz);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                this.status = newStatus;
                System.out.println("Quiz status updated to: " + newStatus);
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to update quiz status!");
            e.printStackTrace();
        }
        return false;
    }
    
    // Method untuk menghitung total score maksimal
    public int getMaxScore() {
        return questions.size() * 10; // Misalnya setiap soal bernilai 10 poin
    }
    
    // Method untuk validasi quiz key
    public static boolean validateQuizKey(String quizKey) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT * FROM quiz WHERE quiz_key = ? AND status = 'ACTIVE'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, quizKey);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Method untuk mendapatkan jumlah peserta
    public int getTotalParticipants() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT COUNT(DISTINCT id_student) as total FROM quiz_session WHERE id_quiz = ? AND status = 'COMPLETED'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, this.idQuiz);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}