package controller;

import db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    public void initialize() {
        loginButton.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> showRegisterMessage());

        usernameField.setOnAction(e -> handleLogin());
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("❌ Error", "Please enter both username and password.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT * FROM customers WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showAlert("✅ Login Successful", "Welcome, " + rs.getString("name") + "!");
                openCustomerDashboard(); // Will be created next
            } else {
                showAlert("❌ Login Failed", "Invalid username or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Database error. Please try again.");
        }
    }

    private void openCustomerDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/customer_dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRegisterMessage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/style/login.css").toExternalForm());
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
