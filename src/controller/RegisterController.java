package controller;

import db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException; // Import SQLException for better error handling

public class RegisterController {

    @FXML
    private TextField nameField, emailField, phoneField, usernameField;

    @FXML
    private PasswordField passwordField, confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink backToLoginLink;

    @FXML
    public void initialize() {
        registerButton.setOnAction(e -> handleRegister());
        backToLoginLink.setOnAction(e -> goBackToLogin());

        // 🔑 Pressing Enter inside any field triggers register
        nameField.setOnAction(e -> handleRegister());
        emailField.setOnAction(e -> handleRegister());
        phoneField.setOnAction(e -> handleRegister());
        usernameField.setOnAction(e -> handleRegister());
        passwordField.setOnAction(e -> handleRegister());
        confirmPasswordField.setOnAction(e -> handleRegister());
    }

    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Basic validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("❌ Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("❌ Error", "Passwords do not match.");
            return;
        }

        // Insert into DB
        try (Connection conn = DatabaseConnection.connect()) {
            // 1️⃣ Insert into customers table (for profile details)
            String sqlCustomer = "INSERT INTO customers (name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt1 = conn.prepareStatement(sqlCustomer);
            stmt1.setString(1, name);
            stmt1.setString(2, email);
            stmt1.setString(3, phone);
            stmt1.setString(4, username);
            stmt1.setString(5, password);
            stmt1.executeUpdate();
            stmt1.close(); // Close the first statement

            // 2️⃣ Insert into users table (for login credentials) with role='customer'
            String sqlUser = "INSERT INTO users (username, password, role) VALUES (?, ?, 'customer')";
            PreparedStatement stmt2 = conn.prepareStatement(sqlUser);
            stmt2.setString(1, username);
            stmt2.setString(2, password);
            stmt2.executeUpdate();
            stmt2.close(); // Close the second statement

            showAlert("✅ Success", "Account registered successfully! You can now log in.");
            clearForm();

        } catch (SQLException ex) { // Catch SQLException specifically for database-related errors
            ex.printStackTrace();
            if (ex.getErrorCode() == 19 || ex.getSQLState().startsWith("23")) { // SQLite unique constraint error code is 19, SQLState for integrity constraint violation starts with 23
                showAlert("❌ Error", "Registration failed. Username might already exist or another database constraint was violated.");
            } else {
                showAlert("❌ Error", "Database error during registration: " + ex.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("❌ Error", "An unexpected error occurred during registration.");
        }
    }

    private void goBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/login.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}