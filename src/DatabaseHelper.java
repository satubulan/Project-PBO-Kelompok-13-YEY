import java.sql.*;

public class DatabaseHelper {
    private static final String DB_NAME = "quizly_db";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String URL_WITHOUT_DB = "jdbc:mysql://localhost:3306/";  // ← TANPA database
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";  // ← Sesuaikan password kamu
    private static Connection connection;
    
    // Method untuk CREATE DATABASE kalau belum ada
    public static void createDatabaseIfNotExists() {
        try {
            // Connect tanpa specify database dulu
            Connection conn = DriverManager.getConnection(URL_WITHOUT_DB, USERNAME, PASSWORD);
            Statement stmt = conn.createStatement();
            
            // Create database
            String createDB = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDB);
            
            System.out.println("✓ Database '" + DB_NAME + "' checked/created successfully!");
            
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Failed to create database!");
            e.printStackTrace();
        }
    }
    
    // Singleton pattern untuk koneksi
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("✓ Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Connection failed!");
            e.printStackTrace();
        }
        return connection;
    }
    
    // Buat semua tabel yang dibutuhkan
    public static void createTables() {
        try {
            Connection conn = getConnection();
            
            if (conn == null) {
                System.out.println("⚠️  Cannot create tables: Connection is null!");
                return;
            }
            
            Statement stmt = conn.createStatement();
            
            // Tabel User
            String createUserTable = "CREATE TABLE IF NOT EXISTS users (" +
                "id_user INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "role ENUM('TEACHER', 'STUDENT') NOT NULL" +
                ")";
            stmt.execute(createUserTable);
            
            // Tabel Quiz
            String createQuizTable = "CREATE TABLE IF NOT EXISTS quiz (" +
                "id_quiz INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(100) NOT NULL," +
                "quiz_key VARCHAR(10) UNIQUE NOT NULL," +
                "time_limit INT NOT NULL," +
                "status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'," +
                "id_teacher INT NOT NULL," +
                "FOREIGN KEY (id_teacher) REFERENCES users(id_user) ON DELETE CASCADE" +
                ")";
            stmt.execute(createQuizTable);
            
            // Tabel Question
            String createQuestionTable = "CREATE TABLE IF NOT EXISTS question (" +
                "id_question INT AUTO_INCREMENT PRIMARY KEY," +
                "id_quiz INT NOT NULL," +
                "question_text TEXT NOT NULL," +
                "option_a VARCHAR(255) NOT NULL," +
                "option_b VARCHAR(255) NOT NULL," +
                "option_c VARCHAR(255) NOT NULL," +
                "option_d VARCHAR(255) NOT NULL," +
                "correct_answer CHAR(1) NOT NULL," +
                "FOREIGN KEY (id_quiz) REFERENCES quiz(id_quiz) ON DELETE CASCADE" +
                ")";
            stmt.execute(createQuestionTable);
            
            // Tabel Quiz_Session
            String createQuizSessionTable = "CREATE TABLE IF NOT EXISTS quiz_session (" +
                "id_session INT AUTO_INCREMENT PRIMARY KEY," +
                "id_student INT NOT NULL," +
                "id_quiz INT NOT NULL," +
                "start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "end_time TIMESTAMP NULL," +
                "status ENUM('ONGOING', 'COMPLETED') DEFAULT 'ONGOING'," +
                "FOREIGN KEY (id_student) REFERENCES users(id_user) ON DELETE CASCADE," +
                "FOREIGN KEY (id_quiz) REFERENCES quiz(id_quiz) ON DELETE CASCADE" +
                ")";
            stmt.execute(createQuizSessionTable);
            
            // Tabel User_Answer
            String createUserAnswerTable = "CREATE TABLE IF NOT EXISTS user_answer (" +
                "id_answer INT AUTO_INCREMENT PRIMARY KEY," +
                "id_session INT NOT NULL," +
                "id_question INT NOT NULL," +
                "answer CHAR(1)," +
                "is_correct BOOLEAN DEFAULT FALSE," +
                "FOREIGN KEY (id_session) REFERENCES quiz_session(id_session) ON DELETE CASCADE," +
                "FOREIGN KEY (id_question) REFERENCES question(id_question) ON DELETE CASCADE" +
                ")";
            stmt.execute(createUserAnswerTable);
            
            // Tabel Result
            String createResultTable = "CREATE TABLE IF NOT EXISTS result (" +
                "id_result INT AUTO_INCREMENT PRIMARY KEY," +
                "id_session INT NOT NULL," +
                "total_score INT NOT NULL," +
                "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (id_session) REFERENCES quiz_session(id_session) ON DELETE CASCADE" +
                ")";
            stmt.execute(createResultTable);
            
            // Tabel Leaderboard
            String createLeaderboardTable = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                "id_leaderboard INT AUTO_INCREMENT PRIMARY KEY," +
                "id_quiz INT NOT NULL," +
                "id_student INT NOT NULL," +
                "score INT NOT NULL," +
                "rank INT NOT NULL," +
                "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (id_quiz) REFERENCES quiz(id_quiz) ON DELETE CASCADE," +
                "FOREIGN KEY (id_student) REFERENCES users(id_user) ON DELETE CASCADE" +
                ")";
            stmt.execute(createLeaderboardTable);
            
            System.out.println("✓ All tables created successfully!");
            stmt.close();
            
        } catch (SQLException e) {
            System.out.println("❌ Error creating tables!");
            e.printStackTrace();
        }
    }
    
    // Close connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Helper method untuk execute query
    public static ResultSet executeQuery(String query) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Helper method untuk execute update
    public static int executeUpdate(String query) {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(query);
            stmt.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}