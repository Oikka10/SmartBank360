package controller;

import db.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Hyperlink registerLink;

    @FXML private ImageView eyeIcon;
    @FXML private TextField visiblePasswordField;
    private boolean isPasswordVisible = false;

    // ADDED: FXML declaration for bankLogo
    @FXML private ImageView bankLogo;

    @FXML
    public void initialize() {
        // ADDED: Logo loading logic
        try {
            bankLogo.setImage(new Image(getClass().getResource("/images/bank_logo.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        // existing login button logic...
        loginButton.setOnAction(e -> handleLogin());
        registerLink.setOnAction(e -> showRegisterMessage());

        // Ensure Enter key triggers login for username field
        usernameField.setOnAction(e -> handleLogin());

        // Ensure Enter key triggers login for password field
        passwordField.setOnAction(e -> handleLogin());

        // Ensure Enter key triggers login for visible password field when it's active
        visiblePasswordField.setOnAction(e -> handleLogin());

        // Set initial eye icon
        eyeIcon.setImage(new Image(getClass().getResource("/images/eye_closed.png").toExternalForm()));

        // Toggle password visibility on icon click
        eyeIcon.setOnMouseClicked(e -> togglePasswordVisibility());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password;

        if (isPasswordVisible) {
            password = visiblePasswordField.getText();
        } else {
            password = passwordField.getText();
        }

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("❌ Error", "Please enter both username and password.");
            return;
        }

        try {
            Connection conn = DatabaseConnection.connect();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                switch (role) {
                    case "admin":
                        loadAdminPanel();
                        break;
                    case "officer":
                        loadOfficerPanel();
                        break;
                    case "customer":
                        loadCustomerPanel(username);
                        break;
                    default:
                        showAlert("❌ Error", "Invalid user role.");
                }
            } else {
                showAlert("❌ Login Failed", "Invalid username or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Database error. Please try again.");
        }
    }

    private void loadAdminPanel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/admin_dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartBank360 - Admin Panel");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Could not open admin panel.");
        }
    }

    private void loadOfficerPanel() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/officer_dashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartBank360 - Officer Panel");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Could not open officer panel.");
        }
    }

    private void loadCustomerPanel(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer_dashboard.fxml"));
            Parent root = loader.load();

            CustomerDashboardController controller = loader.getController();
            controller.setCurrentUsername(username);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("SmartBank360 - Customer Panel");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Could not open customer panel.");
        }
    }

    private void showRegisterMessage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
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

    private void togglePasswordVisibility() {
        if (!isPasswordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            isPasswordVisible = true;
            eyeIcon.setImage(new Image(getClass().getResource("/images/eye_open.png").toExternalForm()));
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            isPasswordVisible = false;
            eyeIcon.setImage(new Image(getClass().getResource("/images/eye_closed.png").toExternalForm()));
        }
    }
}