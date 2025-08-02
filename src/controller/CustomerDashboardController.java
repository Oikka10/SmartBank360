package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.DatabaseConnector;
import db.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import controller.CardRequestController;
import controller.TransactionHistoryController;
import controller.ViewBalanceController;
import controller.LoanRequestController;
import controller.CustomerProfileController;

import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;


public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea activityArea;
    @FXML private VBox dashboardView, profileView, loanView, cardView, historyView, createAccountView, viewBalanceView, sendMoneyView;
    @FXML private Button logoutBtn;
    @FXML private Button profileBtn;

    @FXML private ComboBox<String> accountTypeBox, branchBox, currencyBox;
    @FXML private TextField depositField, nomineeField, mobileField, nidField, addressField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private CheckBox cardCheck;
    @FXML private Label messageLabel;
    @FXML private Button submitBtn;

    @FXML private ComboBox<String> transactionTypeBox;
    @FXML private TextField recipientField, amountField;
    @FXML private Button sendBtn;
    @FXML private Label sendMoneyMessageLabel;

    @FXML private Label notifDot;

    @FXML private VBox notificationsView;
    @FXML private ListView<String> notificationList;

    @FXML private TextArea loanArea, sendMoneyArea, createAccountArea;

    @FXML private ImageView bankLogo;
    @FXML private Label accNumberLabel;
    @FXML private Label accTypeLabel;
    @FXML private Label balanceLabel;

    // This is the crucial FXML field linking to your ListView
    @FXML private BarChart<String, Number> statChart;
    @FXML private ListView<String> recentTransactions; // Ensure this matches fx:id="recentTransactions"

    private String currentUsername;

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
        if (welcomeLabel != null) {
            welcomeLabel.setText("🎉 Welcome, " + username);
        }
        showDashboard();
        checkUnreadNotifications();
    }

    @FXML
    public void initialize() {
        if (logoutBtn != null) {
            logoutBtn.getStyleClass().add("sidebar-button");
            logoutBtn.getStyleClass().add("logout");
        }
        if (profileBtn != null) {
            profileBtn.setOnAction(e -> showCustomerProfile());
        }
        checkUnreadNotifications();

        try {
            bankLogo.setImage(new Image(getClass().getResource("/images/bank_logo.png").toExternalForm()));
        } catch (Exception e) {
            System.out.println("Logo not found: " + e.getMessage());
        }

        // This is where loadDummyTransactions is called
        loadDummyTransactions();
        loadDashboardAccountInfo();

        if (statChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Jan", 10000));
            series.getData().add(new XYChart.Data<>("Feb", 7500));
            series.getData().add(new XYChart.Data<>("Mar", 5000));
            series.getData().add(new XYChart.Data<>("Arp", 9000));
            series.getData().add(new XYChart.Data<>("May", 8000));
            series.getData().add(new XYChart.Data<>("Jun", 4000));
            series.getData().add(new XYChart.Data<>("Jul", 5000));
            series.getData().add(new XYChart.Data<>("Aug", 2000));
            statChart.getData().add(series);
        }
    }

    @FXML
    public void showDashboard() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
            dashboardView.setManaged(true);
            loadDashboardAccountInfo();
            // loadRecentTransactions(); // You might switch back to this for real data later
            loadDummyTransactions(); // Ensure dummy data is loaded when navigating to dashboard
            checkUnreadNotifications();
        }
    }

    @FXML
    public void showCreateAccount() {
        hideAllViews();
        if (createAccountView != null) {
            createAccountView.setVisible(true);
            createAccountView.setManaged(true);
            setupCreateAccountForm();
            checkUnreadNotifications();
        }
    }

    @FXML
    public void showViewBalance() {
        hideAllViews();
        if (viewBalanceView != null) {
            viewBalanceView.setVisible(true);
            viewBalanceView.setManaged(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/view_balance.fxml"));
                VBox balanceUI = loader.load();

                ViewBalanceController controller = loader.getController();
                if (controller != null) {
                    controller.setUsername(currentUsername);
                } else {
                    System.err.println("Error: ViewBalanceController is null after loading FXML.");
                }

                viewBalanceView.getChildren().setAll(balanceUI);
                checkUnreadNotifications();

            } catch (Exception e) {
                System.err.println("Error loading view_balance.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void showSendMoney() {
        hideAllViews();
        if (sendMoneyView != null) {
            sendMoneyView.setVisible(true);
            sendMoneyView.setManaged(true);
            setupSendMoneyForm();
            checkUnreadNotifications();
        }
    }

    @FXML
    public void showProfile() {
        showCustomerProfile();
    }

    @FXML
    private void showCustomerProfile() {
        hideAllViews();

        if (profileView != null) {
            profileView.setVisible(true);
            profileView.setManaged(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/customer_profile.fxml"));
                BorderPane profilePane = loader.load();

                CustomerProfileController controller = loader.getController();
                if (controller != null) {
                    controller.setUsername(currentUsername);
                } else {
                    System.err.println("Error: CustomerProfileController is null after loading FXML.");
                }

                profileView.getChildren().setAll(profilePane);
                checkUnreadNotifications();
            } catch (Exception e) {
                System.out.println("Error loading customer_profile.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void showNotifications() {
        hideAllViews();
        if (notificationsView != null) {
            notificationsView.setVisible(true);
            notificationsView.setManaged(true);

            loadNotifications();

            try (Connection conn = DatabaseConnector.getConnection()) {
                PreparedStatement markRead = conn.prepareStatement(
                        "UPDATE notifications SET is_read=1 WHERE username=?"
                );
                markRead.setString(1, currentUsername);
                markRead.executeUpdate();

                if (notifDot != null) {
                    notifDot.setVisible(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error: notificationsView is null. Check FXML and controller initialization.");
        }
    }

    private void loadNotifications() {
        notificationList.getItems().clear();
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT message, created_at FROM notifications WHERE username=? ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String msg = "🟢 [" + rs.getString("created_at") + "] " + rs.getString("message");
                notificationList.getItems().add(msg);
            }
            if (notificationList.getItems().isEmpty()) {
                notificationList.getItems().add("✅ No notifications yet.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            notificationList.getItems().add("⚠️ Could not load notifications.");
        }
    }

    private void markNotificationsAsRead() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE notifications SET is_read=TRUE WHERE username=?");
            stmt.setString(1, currentUsername);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showTransactionHistory() {
        hideAllViews();
        if (historyView != null) {
            historyView.setVisible(true);
            historyView.setManaged(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/transaction_history.fxml"));
                VBox txHistoryUI = loader.load();

                TransactionHistoryController controller = loader.getController();
                if (controller != null) {
                    controller.setUsername(currentUsername);
                } else {
                    System.err.println("Error: TransactionHistoryController is null after loading FXML.");
                }

                historyView.getChildren().setAll(txHistoryUI);
                checkUnreadNotifications();

            } catch (Exception e) {
                System.err.println("Error loading transaction_history.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void showCardRequest() {
        hideAllViews();
        if (cardView != null) {
            cardView.setVisible(true);
            cardView.setManaged(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/card_request.fxml"));
                VBox cardUI = loader.load();

                CardRequestController controller = loader.getController();
                if (controller != null) {
                    controller.setUsername(currentUsername);
                } else {
                    System.err.println("Error: CardRequestController is null after loading FXML.");
                }

                cardView.getChildren().setAll(cardUI);
                checkUnreadNotifications();

            } catch (Exception e) {
                System.err.println("Error loading card_request.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void showLoanRequest() {
        hideAllViews();
        if (loanView != null) {
            loanView.setVisible(true);
            loanView.setManaged(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loan_request.fxml"));
                VBox loanUI = loader.load();

                LoanRequestController controller = loader.getController();
                if (controller != null) {
                    controller.setUsername(currentUsername);
                } else {
                    System.err.println("Error: LoanRequestController is null after loading FXML.");
                }

                loanView.getChildren().setAll(loanUI);
                checkUnreadNotifications();

            } catch (Exception e) {
                System.err.println("Error loading loan_request.fxml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setTitle("SmartBank360 - Login");
        } catch (IOException e) {
            System.err.println("Error loading login.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Error", "Could not load login screen.");
        }
    }

    private void setupCreateAccountForm() {
        if (accountTypeBox.getItems().isEmpty()) {
            accountTypeBox.getItems().setAll("Savings", "Current", "Student");
            branchBox.getItems().setAll("Dhaka", "Chittagong", "Rajshahi", "Sylhet");
            currencyBox.getItems().setAll("BDT", "USD", "EUR");
        }
        submitBtn.setOnAction(e -> handleAccountCreation());
        messageLabel.setText("");
        clearCreateForm();
    }

    private void handleAccountCreation() {
        String accType = accountTypeBox.getValue();
        String branch = branchBox.getValue();
        String currency = currencyBox.getValue();
        String depositText = depositField.getText();
        String nominee = nomineeField.getText();
        String mobile = mobileField.getText();
        String nid = nidField.getText();
        String address = addressField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        boolean card = cardCheck.isSelected();

        if (accType == null || branch == null || currency == null || depositText.isEmpty() ||
                nominee.isEmpty() || mobile.isEmpty() || nid.isEmpty() || address.isEmpty() ||
                email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("⚠️ Please fill in all fields!");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("❌ Passwords do not match!");
            return;
        }

        try {
            double deposit = Double.parseDouble(depositText);
            if (deposit < 1000) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("⚠️ Minimum initial deposit is ৳1000!");
                return;
            }

            String accountNumber = "Pending";

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnector.getConnection();
                String sql = "INSERT INTO accounts (account_type, branch, currency, deposit, card_needed, nominee, mobile, nid, address, email, password, account_number, status, username) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, accType);
                stmt.setString(2, branch);
                stmt.setString(3, currency);
                stmt.setDouble(4, deposit);
                stmt.setBoolean(5, card);
                stmt.setString(6, nominee);
                stmt.setString(7, mobile);
                stmt.setString(8, nid);
                stmt.setString(9, address);
                stmt.setString(10, email);
                stmt.setString(11, password);
                stmt.setString(12, accountNumber);
                stmt.setString(13, "Pending");
                stmt.setString(14, currentUsername);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("✅ Account request submitted successfully! Please wait for approval.");
                    clearCreateForm();
                    loadDashboardInfo();
                    checkUnreadNotifications();
                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("❌ Account request failed to submit.");
                }
            } finally {
                if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }

        } catch (NumberFormatException ex) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("❌ Invalid deposit amount. Please enter a number.");
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("❌ An error occurred: " + ex.getMessage());
        }
    }

    private void clearCreateForm() {
        accountTypeBox.setValue(null);
        branchBox.setValue(null);
        currencyBox.setValue(null);
        depositField.clear();
        cardCheck.setSelected(false);
        nomineeField.clear();
        mobileField.clear();
        nidField.clear();
        addressField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        messageLabel.setText("");
    }

    private void setupSendMoneyForm() {
        if (transactionTypeBox.getItems().isEmpty()) {
            transactionTypeBox.getItems().addAll("Send Money", "Mobile Recharge", "Credit Card Payment");
            transactionTypeBox.setValue("Send Money");
        }
        sendBtn.setOnAction(e -> processTransaction());
        sendMoneyMessageLabel.setText("");
        recipientField.clear();
        amountField.clear();
    }

    private void processTransaction() {
        String type = transactionTypeBox.getValue();
        String to = recipientField.getText();
        String amountText = amountField.getText();

        sendMoneyMessageLabel.setStyle("-fx-text-fill: red;");

        if (to.isEmpty() || amountText.isEmpty()) {
            sendMoneyMessageLabel.setText("⚠️ Please fill all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                sendMoneyMessageLabel.setText("❌ Invalid amount. Amount must be positive.");
                return;
            }

            Connection conn = null;
            PreparedStatement stmt = null;
            try {
                conn = DatabaseConnection.connect();

                double currentBalance = 0.0;
                String checkBalanceSql = "SELECT balance FROM accounts WHERE username = ? AND status = 'Approved'";
                try (PreparedStatement balanceStmt = conn.prepareStatement(checkBalanceSql)) {
                    balanceStmt.setString(1, currentUsername);
                    ResultSet rs = balanceStmt.executeQuery();
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance");
                    } else {
                        sendMoneyMessageLabel.setText("❌ No active account found to perform transaction.");
                        return;
                    }
                }

                if (currentBalance < amount) {
                    sendMoneyMessageLabel.setText("❌ Insufficient balance.");
                    return;
                }

                String updateBalanceSql = "UPDATE accounts SET balance = balance - ? WHERE username = ? AND status = 'Approved'";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql)) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, currentUsername);
                    updateStmt.executeUpdate();
                }


                String sql = "INSERT INTO transactions (username, type, recipient, amount, datetime) VALUES (?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, currentUsername);
                stmt.setString(2, type);
                stmt.setString(3, to);
                stmt.setDouble(4, amount);
                stmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    sendMoneyMessageLabel.setStyle("-fx-text-fill: green;");
                    sendMoneyMessageLabel.setText("✅ Transaction successful!");
                    recipientField.clear();
                    amountField.clear();
                    loadDashboardAccountInfo();
                    checkUnreadNotifications();
                } else {
                    sendMoneyMessageLabel.setStyle("-fx-text-fill: red;");
                    sendMoneyMessageLabel.setText("❌ Transaction failed to record.");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                sendMoneyMessageLabel.setText("❌ Database error during transaction: " + ex.getMessage());
            } finally {
                if (stmt != null) try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
                if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }

        } catch (NumberFormatException ex) {
            sendMoneyMessageLabel.setText("❌ Amount must be a valid number.");
        }
    }

    private void loadDashboardInfo() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT status FROM accounts WHERE username = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                statusLabel.setText(rs.getString("status"));
            } else {
                statusLabel.setText("None");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error");
        }
    }

    private void loadDashboardAccountInfo() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT account_number, account_type, balance FROM accounts WHERE username=? AND status='Approved' ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                accNumberLabel.setText(rs.getString("account_number"));
                accTypeLabel.setText(rs.getString("account_type") + " Account");
                balanceLabel.setText("৳ " + String.format("%.2f", rs.getDouble("balance")));
            } else {
                accNumberLabel.setText("N/A");
                accTypeLabel.setText("No Account");
                balanceLabel.setText("৳ 0.00");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadRecentTransactions() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT type, amount, datetime FROM transactions WHERE username=? ORDER BY datetime DESC LIMIT 5");
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();

            recentTransactions.getItems().clear();

            while (rs.next()) {
                String type = rs.getString("type");
                double amount = rs.getDouble("amount");
                String color = type.equalsIgnoreCase("Credit") ? "green" : "red";
                String line = String.format("%s %.2f BDT | %s",
                        type.equals("Credit") ? "🟢" : "🔴", amount, rs.getString("datetime"));

                recentTransactions.getItems().add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAllViews() {
        VBox[] views = {
                dashboardView, profileView, loanView, cardView, historyView,
                createAccountView, viewBalanceView, sendMoneyView, notificationsView
        };
        for (VBox view : views) {
            if (view != null) {
                view.setVisible(false);
                view.setManaged(false);
            }
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void checkUnreadNotifications() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM notifications WHERE username=? AND is_read=0"
            );
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (notifDot != null) {
                    notifDot.setVisible(rs.getInt(1) > 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDummyTransactions() {
        recentTransactions.getItems().clear();

        recentTransactions.getItems().add("🟢 + ৳2000 Credited from Rahim");
        recentTransactions.getItems().add("🔴 - ৳1500 Debited to Akash");
        recentTransactions.getItems().add("🟢 + ৳5000 Credited from Bank Interest");

        recentTransactions.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.contains("🔴")) {
                        setStyle("-fx-text-fill: red; -fx-font-size: 14; -fx-font-weight: bold;");
                    } else if (item.contains("🟢")) {
                        setStyle("-fx-text-fill: green; -fx-font-size: 14; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: black; -fx-font-size: 13;");
                    }
                }
            }
        });
    }
}