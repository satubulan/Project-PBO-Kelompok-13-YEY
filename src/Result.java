import java.sql.*;

public class Result {
    private int idResult;
    private int idSession;
    private int totalScore;
    private Timestamp completedAt;
    
    // Constructor
    public Result() {}
    
    public Result(int idSession, int totalScore) {
        this.idSession = idSession;
        this.totalScore = totalScore;
    }
    
    public Result(int idResult, int idSession, int totalScore, Timestamp completedAt) {
        this.idResult = idResult;
        this.idSession = idSession;
        this.totalScore = totalScore;
        this.completedAt = completedAt;
    }
    
    // Getters and Setters (Encapsulation)
    public int getIdResult() {
        return idResult;
    }
    
    public void setIdResult(int idResult) {
        this.idResult = idResult;
    }
    
    public int getIdSession() {
        return idSession;
    }
    
    public void setIdSession(int idSession) {
        this.idSession = idSession;
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }
    
    public Timestamp getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
    
    // Method untuk save result ke database
    public boolean saveToDatabase() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "INSERT INTO result (id_session, total_score) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, idSession);
            stmt.setInt(2, totalScore);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.idResult = rs.getInt(1);
                    System.out.println("Result saved successfully!");
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to save result!");
            e.printStackTrace();
        }
        return false;
    }
    
    // Static method untuk get result by session id
    public static Result getResultBySession(int sessionId) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT * FROM result WHERE id_session = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, sessionId);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Result(
                    rs.getInt("id_result"),
                    rs.getInt("id_session"),
                    rs.getInt("total_score"),
                    rs.getTimestamp("completed_at")
                );
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to get result!");
            e.printStackTrace();
        }
        return null;
    }
    
    // Method untuk display result detail
    public void displayResultDetail() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Get quiz info
            String quizQuery = "SELECT q.title, u.username " +
                              "FROM quiz_session qs " +
                              "JOIN quiz q ON qs.id_quiz = q.id_quiz " +
                              "JOIN users u ON qs.id_student = u.id_user " +
                              "WHERE qs.id_session = ?";
            PreparedStatement stmt = conn.prepareStatement(quizQuery);
            stmt.setInt(1, idSession);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String quizTitle = rs.getString("title");
                String username = rs.getString("username");
                
                System.out.println("\n===== RESULT DETAIL =====");
                System.out.println("Quiz: " + quizTitle);
                System.out.println("Student: " + username);
                System.out.println("Score: " + totalScore);
                System.out.println("Completed At: " + completedAt);
                System.out.println("========================\n");
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to display result detail!");
            e.printStackTrace();
        }
    }
    
    // Method untuk get rank
    public int getRank() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Get quiz id from session
            String getQuizQuery = "SELECT id_quiz FROM quiz_session WHERE id_session = ?";
            PreparedStatement getQuizStmt = conn.prepareStatement(getQuizQuery);
            getQuizStmt.setInt(1, idSession);
            ResultSet quizRs = getQuizStmt.executeQuery();
            
            if (quizRs.next()) {
                int quizId = quizRs.getInt("id_quiz");
                
                // Get student id
                String getStudentQuery = "SELECT id_student FROM quiz_session WHERE id_session = ?";
                PreparedStatement getStudentStmt = conn.prepareStatement(getStudentQuery);
                getStudentStmt.setInt(1, idSession);
                ResultSet studentRs = getStudentStmt.executeQuery();
                
                if (studentRs.next()) {
                    int studentId = studentRs.getInt("id_student");
                    
                    // Get rank from leaderboard
                    String rankQuery = "SELECT rank FROM leaderboard WHERE id_quiz = ? AND id_student = ?";
                    PreparedStatement rankStmt = conn.prepareStatement(rankQuery);
                    rankStmt.setInt(1, quizId);
                    rankStmt.setInt(2, studentId);
                    ResultSet rankRs = rankStmt.executeQuery();
                    
                    if (rankRs.next()) {
                        return rankRs.getInt("rank");
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Method untuk compare hasil dengan student lain
    public void compareWithOthers() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Get quiz id
            String getQuizQuery = "SELECT id_quiz FROM quiz_session WHERE id_session = ?";
            PreparedStatement getQuizStmt = conn.prepareStatement(getQuizQuery);
            getQuizStmt.setInt(1, idSession);
            ResultSet quizRs = getQuizStmt.executeQuery();
            
            if (quizRs.next()) {
                int quizId = quizRs.getInt("id_quiz");
                
                // Get statistics
                String statsQuery = "SELECT " +
                                   "COUNT(*) as total_participants, " +
                                   "AVG(l.score) as avg_score, " +
                                   "MAX(l.score) as max_score, " +
                                   "MIN(l.score) as min_score " +
                                   "FROM leaderboard l WHERE l.id_quiz = ?";
                PreparedStatement statsStmt = conn.prepareStatement(statsQuery);
                statsStmt.setInt(1, quizId);
                ResultSet statsRs = statsStmt.executeQuery();
                
                if (statsRs.next()) {
                    System.out.println("\n===== COMPARISON =====");
                    System.out.println("Your Score: " + totalScore);
                    System.out.println("Average Score: " + String.format("%.2f", statsRs.getDouble("avg_score")));
                    System.out.println("Highest Score: " + statsRs.getInt("max_score"));
                    System.out.println("Lowest Score: " + statsRs.getInt("min_score"));
                    System.out.println("Total Participants: " + statsRs.getInt("total_participants"));
                    System.out.println("Your Rank: #" + getRank());
                    System.out.println("=====================\n");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to compare results!");
            e.printStackTrace();
        }
    }
}