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
        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "INSERT INTO customers (name, email, phone, username, password) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            stmt.setString(5, password); // Optional: hash in future

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("✅ Success", "Account registered successfully!");
                clearForm();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("❌ Error", "Registration failed.");
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
