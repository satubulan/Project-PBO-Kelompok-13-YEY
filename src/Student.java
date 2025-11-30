import java.sql.*;
import java.util.Scanner;

public class Student extends User {
    
    public Student() {
        super();
    }
    
    public Student(int idUser, String username, String password, String role) {
        super(idUser, username, password, role);
    }
    
    @Override
    public void showDashboard() {
        System.out.println("===== STUDENT DASHBOARD =====");
        System.out.println("Welcome, " + getUsername() + "!");
        System.out.println("1. Join Quiz");
        System.out.println("2. Check My Score");
        System.out.println("3. View Leaderboard");
        System.out.println("4. Logout");
    }
    
    // Method untuk join quiz dengan quiz key
    public Quiz joinQuiz(String quizKey) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Cari quiz berdasarkan key
            String query = "SELECT * FROM quiz WHERE quiz_key = ? AND status = 'ACTIVE'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, quizKey);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id_quiz"),
                    rs.getString("title"),
                    rs.getString("quiz_key"),
                    rs.getInt("time_limit"),
                    rs.getString("status"),
                    rs.getInt("id_teacher")
                );
                
                // Load questions
                quiz.loadQuestionsFromDatabase();
                
                System.out.println("Quiz found: " + quiz.getTitle());
                return quiz;
            } else {
                System.out.println("Quiz not found or inactive!");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to join quiz!");
            e.printStackTrace();
        }
        return null;
    }
    
    // Method untuk mengerjakan quiz
    public void takeQuiz(Quiz quiz) {
        if (quiz == null) {
            System.out.println("No quiz to take!");
            return;
        }
        
        // Buat quiz session
        QuizSession session = new QuizSession(this, quiz);
        session.startQuiz();
    }
    
    // Method untuk cek score student
    public void checkMyScore() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT q.title, r.total_score, r.completed_at " +
                          "FROM result r " +
                          "JOIN quiz_session qs ON r.id_session = qs.id_session " +
                          "JOIN quiz q ON qs.id_quiz = q.id_quiz " +
                          "WHERE qs.id_student = ? " +
                          "ORDER BY r.completed_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, getIdUser());
            
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n===== MY SCORES =====");
            System.out.printf("%-30s %-10s %-20s\n", "Quiz Title", "Score", "Completed At");
            System.out.println("------------------------------------------------------------------");
            
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                System.out.printf("%-30s %-10d %-20s\n",
                    rs.getString("title"),
                    rs.getInt("total_score"),
                    rs.getTimestamp("completed_at")
                );
            }
            
            if (!hasResults) {
                System.out.println("You haven't completed any quiz yet!");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to fetch scores!");
            e.printStackTrace();
        }
    }
    
    // Method untuk melihat leaderboard quiz tertentu
    public void viewLeaderboard(String quizKey) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Get quiz id from key
            String getQuizQuery = "SELECT id_quiz FROM quiz WHERE quiz_key = ?";
            PreparedStatement getQuizStmt = conn.prepareStatement(getQuizQuery);
            getQuizStmt.setString(1, quizKey);
            ResultSet quizRs = getQuizStmt.executeQuery();
            
            if (quizRs.next()) {
                int quizId = quizRs.getInt("id_quiz");
                
                String query = "SELECT u.username, l.score, l.rank, l.completed_at " +
                              "FROM leaderboard l " +
                              "JOIN users u ON l.id_student = u.id_user " +
                              "WHERE l.id_quiz = ? " +
                              "ORDER BY l.rank ASC";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, quizId);
                
                ResultSet rs = stmt.executeQuery();
                
                System.out.println("\n===== LEADERBOARD =====");
                System.out.printf("%-5s %-20s %-10s %-20s\n", "Rank", "Username", "Score", "Completed At");
                System.out.println("-----------------------------------------------------------");
                
                while (rs.next()) {
                    System.out.printf("%-5d %-20s %-10d %-20s\n",
                        rs.getInt("rank"),
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getTimestamp("completed_at")
                    );
                }
            } else {
                System.out.println("Quiz not found!");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to fetch leaderboard!");
            e.printStackTrace();
        }
    }
    
    // Method untuk melihat semua quiz yang pernah dikerjakan
    public void viewMyQuizHistory() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT DISTINCT q.title, q.quiz_key, qs.start_time, qs.status " +
                          "FROM quiz_session qs " +
                          "JOIN quiz q ON qs.id_quiz = q.id_quiz " +
                          "WHERE qs.id_student = ? " +
                          "ORDER BY qs.start_time DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, getIdUser());
            
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n===== MY QUIZ HISTORY =====");
            while (rs.next()) {
                System.out.println("Title: " + rs.getString("title"));
                System.out.println("Key: " + rs.getString("quiz_key"));
                System.out.println("Started At: " + rs.getTimestamp("start_time"));
                System.out.println("Status: " + rs.getString("status"));
                System.out.println("------------------------");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to fetch quiz history!");
            e.printStackTrace();
        }
    }
}