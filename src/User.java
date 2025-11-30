import java.sql.*;

public abstract class User {
    private int idUser;
    private String username;
    private String password;
    private String role;
    
    // Constructor
    public User() {}
    
    public User(int idUser, String username, String password, String role) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    // Getters and Setters (Encapsulation)
    public int getIdUser() {
        return idUser;
    }
    
    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Abstract method (harus diimplementasi di subclass)
    public abstract void showDashboard();
    
    // Static method untuk Register
    public static boolean register(String username, String password, String role) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            
            // Cek apakah username sudah ada
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Username already exists!");
                return false;
            }
            
            // Insert user baru
            String insertQuery = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, role);
            
            int result = insertStmt.executeUpdate();
            
            if (result > 0) {
                System.out.println("Registration successful!");
                return true;
            }
            
        } catch (SQLException e) {
            System.out.println("Registration failed!");
            e.printStackTrace();
        }
        return false;
    }
    
    // Static method untuk Login
    public static User login(String username, String password) {
        try {
            Connection conn = DatabaseHelper.getConnection();
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int id = rs.getInt("id_user");
                String role = rs.getString("role");
                
                System.out.println("Login successful! Welcome " + username);
                
                // Return object sesuai role
                if (role.equals("TEACHER")) {
                    return new Teacher(id, username, password, role);
                } else if (role.equals("STUDENT")) {
                    return new Student(id, username, password, role);
                }
            } else {
                System.out.println("Invalid username or password!");
            }
            
        } catch (SQLException e) {
            System.out.println("Login failed!");
            e.printStackTrace();
        }
        return null;
    }
    
    // Method untuk Logout
    public void logout() {
        System.out.println("User " + this.username + " logged out successfully!");
    }
}