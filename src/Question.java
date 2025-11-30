import java.sql.*;

public class Question {
    private int idQuestion;
    private int idQuiz;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    
    // Constructor
    public Question() {}
    
    public Question(String questionText, String optionA, String optionB, 
                   String optionC, String optionD, String correctAnswer) {
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }
    
    public Question(int idQuestion, int idQuiz, String questionText, 
                   String optionA, String optionB, String optionC, 
                   String optionD, String correctAnswer) {
        this.idQuestion = idQuestion;
        this.idQuiz = idQuiz;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctAnswer = correctAnswer;
    }
    
    // Getters and Setters (Encapsulation)
    public int getIdQuestion() {
        return idQuestion;
    }
    
    public void setIdQuestion(int idQuestion) {
        this.idQuestion = idQuestion;
    }
    
    public int getIdQuiz() {
        return idQuiz;
    }
    
    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getOptionA() {
        return optionA;
    }
    
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }
    
    public String getOptionB() {
        return optionB;
    }
    
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }
    
    public String getOptionC() {
        return optionC;
    }
    
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }
    
    public String getOptionD() {
        return optionD;
    }
    
    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    // Method untuk display question
    public void displayQuestion(int questionNumber) {
        System.out.println("\nQuestion " + questionNumber + ": " + questionText);
        System.out.println("A. " + optionA);
        System.out.println("B. " + optionB);
        System.out.println("C. " + optionC);
        System.out.println("D. " + optionD);
    }
    
    // Method untuk validasi jawaban
    public boolean checkAnswer(String answer) {
        if (answer == null) return false;
        return answer.trim().equalsIgnoreCase(correctAnswer.trim());
    }
    
    // Method untuk save question ke database
    public boolean saveToDatabase(int quizId) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "INSERT INTO question (id_quiz, question_text, option_a, option_b, option_c, option_d, correct_answer) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, quizId);
            stmt.setString(2, questionText);
            stmt.setString(3, optionA);
            stmt.setString(4, optionB);
            stmt.setString(5, optionC);
            stmt.setString(6, optionD);
            stmt.setString(7, correctAnswer);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    this.idQuestion = rs.getInt(1);
                    this.idQuiz = quizId;
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to save question!");
            e.printStackTrace();
        }
        return false;
    }
    
    // Method untuk update question
    public boolean updateQuestion() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "UPDATE question SET question_text = ?, option_a = ?, option_b = ?, " +
                          "option_c = ?, option_d = ?, correct_answer = ? WHERE id_question = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, questionText);
            stmt.setString(2, optionA);
            stmt.setString(3, optionB);
            stmt.setString(4, optionC);
            stmt.setString(5, optionD);
            stmt.setString(6, correctAnswer);
            stmt.setInt(7, idQuestion);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Question updated successfully!");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to update question!");
            e.printStackTrace();
        }
        return false;
    }
    
    // Method untuk delete question
    public boolean deleteQuestion() {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "DELETE FROM question WHERE id_question = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, idQuestion);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Question deleted successfully!");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Failed to delete question!");
            e.printStackTrace();
        }
        return false;
    }
}