import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainConsole {
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser = null;
    
    public static void main(String[] args) {
        // Initialize database
        DatabaseHelper.createTables();
        
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║     WELCOME TO QUIZLY APP!         ║");
        System.out.println("║   Interactive Quiz Application     ║");
        System.out.println("╚════════════════════════════════════╝\n");
        
        // Main loop
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                if (currentUser instanceof Teacher) {
                    showTeacherMenu();
                } else if (currentUser instanceof Student) {
                    showStudentMenu();
                }
            }
        }
    }
    
    // Main Menu (Login/Register)
    private static void showMainMenu() {
        System.out.println("\n===== MAIN MENU =====");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegister();
                break;
            case "3":
                System.out.println("Thank you for using Quizly!");
                DatabaseHelper.closeConnection();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option!");
        }
    }
    
    // Handle Login
    private static void handleLogin() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = User.login(username, password);
        
        if (currentUser == null) {
            System.out.println("Login failed! Please try again.");
        }
    }
    
    // Handle Register
    private static void handleRegister() {
        System.out.println("\n===== REGISTER =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.println("Register as:");
        System.out.println("1. Teacher");
        System.out.println("2. Student");
        System.out.print("Choose role: ");
        String roleChoice = scanner.nextLine();
        
        String role = "";
        if (roleChoice.equals("1")) {
            role = "TEACHER";
        } else if (roleChoice.equals("2")) {
            role = "STUDENT";
        } else {
            System.out.println("Invalid role!");
            return;
        }
        
        boolean success = User.register(username, password, role);
        
        if (success) {
            System.out.println("Registration successful! You can now login.");
        }
    }
    
    // Teacher Menu
    private static void showTeacherMenu() {
        Teacher teacher = (Teacher) currentUser;
        teacher.showDashboard();
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                handleCreateQuiz(teacher);
                break;
            case "2":
                teacher.viewMyQuizzes();
                break;
            case "3":
                handleViewLeaderboard(teacher);
                break;
            case "4":
                currentUser.logout();
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option!");
        }
    }
    
    // Handle Create Quiz
    private static void handleCreateQuiz(Teacher teacher) {
        System.out.println("\n===== CREATE QUIZ =====");
        System.out.print("Quiz Title: ");
        String title = scanner.nextLine();
        
        System.out.print("Time Limit per Question (seconds): ");
        int timeLimit = Integer.parseInt(scanner.nextLine());
        
        List<Question> questions = new ArrayList<>();
        int questionNumber = 1;
        
        System.out.println("\n💡 Tip: Type 'DONE' when finished adding questions");
        
        // Loop terus sampai teacher bilang selesai
        while (true) {
            System.out.println("\n--- Question " + questionNumber + " ---");
            System.out.print("Question Text (or type 'DONE' to finish): ");
            String questionText = scanner.nextLine();
            
            // Kalau teacher ketik "DONE", keluar dari loop
            if (questionText.trim().equalsIgnoreCase("DONE")) {
                break;
            }
            
            // Validasi minimal ada text
            if (questionText.trim().isEmpty()) {
                System.out.println("⚠️  Question text cannot be empty!");
                continue;
            }
            
            System.out.print("Option A: ");
            String optionA = scanner.nextLine();
            
            System.out.print("Option B: ");
            String optionB = scanner.nextLine();
            
            System.out.print("Option C: ");
            String optionC = scanner.nextLine();
            
            System.out.print("Option D: ");
            String optionD = scanner.nextLine();
            
            System.out.print("Correct Answer (A/B/C/D): ");
            String correctAnswer = scanner.nextLine().trim().toUpperCase();
            
            // Validasi jawaban harus A/B/C/D
            while (!correctAnswer.matches("[ABCD]")) {
                System.out.print("⚠️  Invalid! Correct Answer must be A, B, C, or D: ");
                correctAnswer = scanner.nextLine().trim().toUpperCase();
            }
            
            Question question = new Question(questionText, optionA, optionB, optionC, optionD, correctAnswer);
            questions.add(question);
            
            System.out.println("✓ Question " + questionNumber + " added! (Total: " + questions.size() + " questions)");
            questionNumber++;
        }
        
        // Validasi harus ada minimal 1 soal
        if (questions.isEmpty()) {
            System.out.println("⚠️  Cannot create quiz without questions!");
            return;
        }
        
        // Konfirmasi sebelum create
        System.out.println("\n========================================");
        System.out.println("Quiz Summary:");
        System.out.println("Title: " + title);
        System.out.println("Time Limit: " + timeLimit + " seconds per question");
        System.out.println("Total Questions: " + questions.size());
        System.out.println("========================================");
        System.out.print("Create this quiz? (Y/N): ");
        String confirm = scanner.nextLine();
        
        if (!confirm.equalsIgnoreCase("Y")) {
            System.out.println("Quiz creation cancelled.");
            return;
        }
        
        Quiz quiz = teacher.createQuiz(title, timeLimit, questions);
        
        if (quiz != null) {
            System.out.println("\n✓ Quiz created successfully!");
            System.out.println("📋 Quiz Key: " + quiz.getQuizKey());
            System.out.println("Share this key with your students!");
        }
    }
    
    // Handle View Leaderboard (Teacher)
    private static void handleViewLeaderboard(Teacher teacher) {
        List<Quiz> quizzes = teacher.viewMyQuizzes();
        
        if (quizzes.isEmpty()) {
            System.out.println("You haven't created any quiz yet!");
            return;
        }
        
        System.out.print("\nEnter Quiz ID to view leaderboard: ");
        int quizId = Integer.parseInt(scanner.nextLine());
        
        teacher.viewLeaderboard(quizId);
    }
    
    // Student Menu
    private static void showStudentMenu() {
        Student student = (Student) currentUser;
        student.showDashboard();
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine();
        
        switch (choice) {
            case "1":
                handleJoinQuiz(student);
                break;
            case "2":
                student.checkMyScore();
                break;
            case "3":
                handleViewLeaderboardStudent(student);
                break;
            case "4":
                currentUser.logout();
                currentUser = null;
                break;
            default:
                System.out.println("Invalid option!");
        }
    }
    
    // Handle Join Quiz
    private static void handleJoinQuiz(Student student) {
        System.out.println("\n===== JOIN QUIZ =====");
        System.out.print("Enter Quiz Key: ");
        String quizKey = scanner.nextLine().toUpperCase();
        
        Quiz quiz = student.joinQuiz(quizKey);
        
        if (quiz != null) {
            quiz.displayQuizInfo();
            System.out.print("Ready to start? (Y/N): ");
            String ready = scanner.nextLine();
            
            if (ready.equalsIgnoreCase("Y")) {
                student.takeQuiz(quiz);
            }
        }
    }
    
    // Handle View Leaderboard (Student)
    private static void handleViewLeaderboardStudent(Student student) {
        System.out.print("Enter Quiz Key: ");
        String quizKey = scanner.nextLine().toUpperCase();
        
        student.viewLeaderboard(quizKey);
    }
}