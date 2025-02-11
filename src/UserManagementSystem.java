import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class UserManagementSystem {
    private JFrame mainFrame;
    private JPanel loginPanel;
    private JPanel signupPanel;
    private JPanel homePanel;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private String currentUser;
    private String currentFullName;

    public UserManagementSystem() {
        initializeUI();
    }

    private void initializeUI() {
        mainFrame = new JFrame("User Management System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 700);
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        createSignupPanel();
        createHomePanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(signupPanel, "signup");
        mainPanel.add(homePanel, "home");

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Welcome back!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBounds(150, 20, 200, 40);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 100, 100, 25);
        JTextField emailField = new JTextField();
        emailField.setBounds(50, 130, 380, 35);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 180, 100, 25);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(50, 210, 340, 35);

        JButton togglePasswordButton = new JButton("👁");
        togglePasswordButton.setBounds(395, 210, 35, 35);
        togglePasswordButton.setFocusPainted(false);
        togglePasswordButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('\u2022');
            }
        });

        JCheckBox rememberMeCheckBox = new JCheckBox("Remember me?");
        rememberMeCheckBox.setBounds(50, 260, 150, 25);
        rememberMeCheckBox.setBackground(Color.WHITE);

        JLabel messageLabel = new JLabel("");
        messageLabel.setBounds(50, 290, 380, 25);
        messageLabel.setForeground(Color.RED);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(50, 330, 380, 40);
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        JButton goToSignupButton = new JButton("Don't have an account? Sign up");
        goToSignupButton.setBounds(50, 390, 380, 30);
        goToSignupButton.setContentAreaFilled(false);
        goToSignupButton.setBorderPainted(false);

        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                if (validateLogin(email, password)) {
                    messageLabel.setText("");
                    JOptionPane.showMessageDialog(mainFrame, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, "home");
                } else {
                    messageLabel.setText("Invalid credentials!");
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        goToSignupButton.addActionListener(e -> cardLayout.show(mainPanel, "signup"));

        loginPanel.add(titleLabel);
        loginPanel.add(emailLabel);
        loginPanel.add(emailField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(togglePasswordButton);
        loginPanel.add(rememberMeCheckBox);
        loginPanel.add(messageLabel);
        loginPanel.add(loginButton);
        loginPanel.add(goToSignupButton);
    }

    private void createSignupPanel() {
        signupPanel = new JPanel();
        signupPanel.setLayout(null);
        signupPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Create account!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBounds(140, 20, 300, 40);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(50, 100, 100, 25);
        JTextField nameField = new JTextField();
        nameField.setBounds(50, 130, 380, 35);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(50, 180, 100, 25);
        JTextField emailField = new JTextField();
        emailField.setBounds(50, 210, 380, 35);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 260, 100, 25);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(50, 290, 340, 35);

        JButton togglePasswordButton = new JButton("👁");
        togglePasswordButton.setBounds(395, 290, 35, 35);
        togglePasswordButton.setFocusPainted(false);
        togglePasswordButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == '\u2022') {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('\u2022');
            }
        });

        JLabel messageLabel = new JLabel("");
        messageLabel.setBounds(50, 330, 380, 25);
        messageLabel.setForeground(Color.RED);

        JButton signupButton = new JButton("Create");
        signupButton.setBounds(50, 370, 380, 40);
        signupButton.setBackground(new Color(70, 130, 180));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFocusPainted(false);

        JButton goToLoginButton = new JButton("Already have an account? Login");
        goToLoginButton.setBounds(50, 430, 380, 30);
        goToLoginButton.setContentAreaFilled(false);
        goToLoginButton.setBorderPainted(false);

        signupButton.addActionListener(e -> {
            String fullName = nameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            try {
                if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    messageLabel.setText("All fields are required!");
                } else {
                    registerUser(fullName, email, password);
                    messageLabel.setText("");
                    JOptionPane.showMessageDialog(mainFrame, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, "login");
                }
            } catch (Exception ex) {
                messageLabel.setText("Error: " + ex.getMessage());
            }
        });

        goToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        signupPanel.add(titleLabel);
        signupPanel.add(nameLabel);
        signupPanel.add(nameField);
        signupPanel.add(emailLabel);
        signupPanel.add(emailField);
        signupPanel.add(passwordLabel);
        signupPanel.add(passwordField);
        signupPanel.add(togglePasswordButton);
        signupPanel.add(messageLabel);
        signupPanel.add(signupButton);
        signupPanel.add(goToLoginButton);
    }

    private void createHomePanel() {
        homePanel = new JPanel();
        homePanel.setLayout(null);
        homePanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome back, " + (currentFullName != null ? currentFullName : "User") + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setBounds(50, 50, 400, 40);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton dashboardButton = new JButton("Student Dashboard");
        dashboardButton.setBounds(150, 150, 200, 40);
        dashboardButton.setBackground(new Color(70, 130, 180));
        dashboardButton.setForeground(Color.WHITE);
        dashboardButton.setFocusPainted(false);
        dashboardButton.addActionListener(e -> {
            StudentManagement studentManagement = new StudentManagement();
            studentManagement.showManagement();
        });

        // Add new Note Management button
        JButton noteButton = new JButton("Note Management");
        noteButton.setBounds(150, 200, 200, 40);
        noteButton.setBackground(new Color(70, 130, 180));
        noteButton.setForeground(Color.WHITE);
        noteButton.setFocusPainted(false);
        noteButton.addActionListener(e -> {
            Note noteManagement = new Note();
            noteManagement.setVisible(true);
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(150, 250, 200, 40);  // Moved down to make room for note button
        logoutButton.setBackground(new Color(220, 20, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> cardLayout.show(mainPanel, "login"));

        homePanel.add(welcomeLabel);
        homePanel.add(dashboardButton);
        homePanel.add(noteButton);  // Add the new button
        homePanel.add(logoutButton);
    }

    private boolean validateLogin(String email, String password) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT full_name FROM users WHERE email = ? AND password = ?")) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentFullName = rs.getString("full_name");
                currentUser = email;
                return true;
            }
            return false;
        }
    }

    private void registerUser(String fullName, String email, String password) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)")) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, password);

            stmt.executeUpdate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserManagementSystem());
    }
}