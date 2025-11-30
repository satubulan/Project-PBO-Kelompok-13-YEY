import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Teacher extends User {
    
    public Teacher() {
        super();
    }
    
    public Teacher(int idUser, String username, String password, String role) {
        super(idUser, username, password, role);
    }
    
    @Override
    public void showDashboard() {
        System.out.println("===== TEACHER DASHBOARD =====");
        System.out.println("Welcome, " + getUsername() + "!");
        System.out.println("1. Create Quiz");
        System.out.println("2. View My Quizzes");
        System.out.println("3. View Leaderboard");
        System.out.println("4. Logout");
    }
    
    // Method untuk membuat quiz baru
    public Quiz createQuiz(String title, int timeLimit, List<Question> questions) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Generate unique quiz key
            String quizKey = generateQuizKey();
            
            // Insert quiz
            String insertQuizQuery = "INSERT INTO quiz (title, quiz_key, time_limit, id_teacher) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(insertQuizQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, title);
            stmt.setString(2, quizKey);
            stmt.setInt(3, timeLimit);
            stmt.setInt(4, getIdUser());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int quizId = rs.getInt(1);
                    
                    // Insert questions
                    for (Question q : questions) {
                        q.saveToDatabase(quizId);
                    }
                    
                    System.out.println("Quiz created successfully!");
                    System.out.println("Quiz Key: " + quizKey);
                    
                    Quiz quiz = new Quiz(quizId, title, quizKey, timeLimit, "ACTIVE", getIdUser());
                    quiz.setQuestions(questions);
                    return quiz;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to create quiz!");
            e.printStackTrace();
        }
        return null;
    }
    
    // Generate quiz key (6 karakter random)
    private String generateQuizKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            key.append(chars.charAt(index));
        }
        return key.toString();
    }
    
    // Method untuk melihat semua quiz yang dibuat
    public List<Quiz> viewMyQuizzes() {
        List<Quiz> quizzes = new ArrayList<>();
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT * FROM quiz WHERE id_teacher = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, getIdUser());
            
            ResultSet rs = stmt.executeQuery();
            
            System.out.println("\n===== MY QUIZZES =====");
            while (rs.next()) {
                Quiz quiz = new Quiz(
                    rs.getInt("id_quiz"),
                    rs.getString("title"),
                    rs.getString("quiz_key"),
                    rs.getInt("time_limit"),
                    rs.getString("status"),
                    rs.getInt("id_teacher")
                );
                quizzes.add(quiz);
                
                System.out.println("Quiz ID: " + quiz.getIdQuiz());
                System.out.println("Title: " + quiz.getTitle());
                System.out.println("Key: " + quiz.getQuizKey());
                System.out.println("Time Limit: " + quiz.getTimeLimit() + " seconds");
                System.out.println("Status: " + quiz.getStatus());
                System.out.println("------------------------");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to fetch quizzes!");
            e.printStackTrace();
        }
        return quizzes;
    }
    
    // Method untuk melihat leaderboard quiz tertentu
    public void viewLeaderboard(int quizId) {
        try {
            Connection conn = DatabaseHelper.getConnection();
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
            
        } catch (SQLException e) {
            System.out.println("Failed to fetch leaderboard!");
            e.printStackTrace();
        }
    }
    
    // Method untuk delete quiz
    public boolean deleteQuiz(int quizId) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "DELETE FROM quiz WHERE id_quiz = ? AND id_teacher = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, quizId);
            stmt.setInt(2, getIdUser());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Quiz deleted successfully!");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to delete quiz!");
            e.printStackTrace();
        }
        return false;
    }
}