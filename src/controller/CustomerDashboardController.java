package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TextArea activityArea;
    @FXML private Button logoutBtn, createAccBtn, viewBalanceBtn, requestServiceBtn, notifBtn;

    private String currentUsername;

    @FXML
    public void initialize() {
        logoutBtn.setOnAction(e -> handleLogout());
        createAccBtn.setOnAction(e -> openCreateAccount());
        viewBalanceBtn.setOnAction(e -> openViewBalance());
        requestServiceBtn.setOnAction(e -> openSendMoney());
        notifBtn.setOnAction(e -> showNotifications());
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        welcomeLabel.setText("🎉 Welcome, " + username);
        loadProfileAndActivity();
    }

    private void loadProfileAndActivity() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE username = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String accNo = rs.getString("account_number");
                String balance = rs.getString("deposit");
                String status = rs.getString("status");
                String type = rs.getString("account_type");
                String branch = rs.getString("branch");

                activityArea.setText(
                        "🔢 Account Number: " + accNo + "\n" +
                                "💼 Account Type: " + type + "\n" +
                                "🏢 Branch: " + branch + "\n" +
                                "💰 Balance: ৳" + balance + "\n" +
                                "📄 Status: " + status
                );
            } else {
                activityArea.setText("❌ No account created yet.\nClick '💼 Create Account' to request one.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            activityArea.setText("❌ Failed to load profile info.");
        }
    }

    @FXML
    private void showBalance() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT deposit FROM accounts WHERE username = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String balance = rs.getString("deposit");
                showAlert("💳 Current Balance", "Your balance is ৳" + balance);
            } else {
                showAlert("❌ No Account Found", "Please request a new account first.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Unable to fetch balance.");
        }
    }

    @FXML
    private void showNotifications() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT status FROM accounts WHERE username = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                showAlert("🔔 Account Status", "Your latest account status is: " + status);
            } else {
                showAlert("ℹ️ Info", "You haven't requested any account yet.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Error", "Could not fetch status.");
        }
    }
    private void openSendMoney() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/send_money.fxml"));
            Parent root = loader.load();

            SendMoneyController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("💳 Send Money");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openLoanRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loan_request.fxml"));
            Parent root = loader.load();

            LoanRequestController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Loan Request");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void openViewBalance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/view_balance.fxml"));
            Parent root = loader.load();

            // Send the current username to the balance controller
            ViewBalanceController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("💳 View Account Balance");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openCardRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/card_request.fxml"));
            Parent root = loader.load();

            CardRequestController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("Card Request");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void openTransactionHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/transaction_history.fxml"));
            Parent root = loader.load();

            // Pass username to transaction history controller
            TransactionHistoryController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("📜 Transaction History");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer_profile.fxml"));
            Parent root = loader.load();

            CustomerProfileController controller = loader.getController();
            controller.setUsername(currentUsername);

            Stage stage = new Stage();
            stage.setTitle("👤 My Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            logoutBtn.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCreateAccount() {
        try {
            Parent createAccountRoot = FXMLLoader.load(getClass().getResource("/view/create_account.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Open New Account");
            stage.setScene(new Scene(createAccountRoot));
            stage.show();
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
