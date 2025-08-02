package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.AccountRequest;
import model.CardRequest;
import model.LoanRequest;
import utils.DatabaseConnector;

import java.io.IOException; // Added for handleLogout if it throws an IOException
import java.sql.*;
import java.util.Optional;
import java.util.Random; // Added for generateAccountNumber

public class OfficerDashboardController {

    @FXML private Label notificationBadge, accStat, loanStat, cardStat;
    @FXML private VBox dashboardSection, accountSection, loanSection, cardSection;
    @FXML private TableView<AccountRequest> accountTable;
    @FXML private TableColumn<AccountRequest, Integer> colId;
    @FXML private TableColumn<AccountRequest, String> colName, colMobile, colType, colBranch;
    @FXML private TableColumn<AccountRequest, Void> colAction;
    @FXML private ComboBox<String> statusFilterCombo;

    @FXML private TableView<LoanRequest> loanTable;
    @FXML private TableColumn<LoanRequest, Integer> loanIdCol;
    @FXML private TableColumn<LoanRequest, String> loanUserCol, loanTypeCol, loanReasonCol, loanStatusCol;
    @FXML private TableColumn<LoanRequest, Double> loanAmountCol;
    @FXML private TableColumn<LoanRequest, Integer> loanDurationCol;
    @FXML private TableColumn<LoanRequest, Void> loanActionCol;

    @FXML private TableView<CardRequest> cardTable;
    @FXML private TableColumn<CardRequest, Integer> cardIdCol;
    @FXML private TableColumn<CardRequest, String> cardUserCol, cardTypeCol, cardMobileCol, cardAddressCol, cardStatusCol;
    @FXML private TableColumn<CardRequest, Void> cardActionCol;

    @FXML private BarChart<String, Number> barChart;
    private final ObservableList<AccountRequest> requestList = FXCollections.observableArrayList();

    @FXML private Label dotAccount;
    @FXML private Label dotLoan;
    @FXML private Label dotCard;


    @FXML
    public void initialize() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "Pending", "Approved", "Rejected"));
        statusFilterCombo.setValue("Pending");
        statusFilterCombo.setOnAction(e -> filterByStatus());

        loadFilteredRequests("Pending");
        loadLoanRequests();
        loadCardRequests();
        updateNotificationCount();
        updateDashboardStats();
    }

    @FXML private void showDashboard() {
        dashboardSection.setVisible(true);
        dashboardSection.setManaged(true);
        accountSection.setVisible(false);
        accountSection.setManaged(false);
        loanSection.setVisible(false);
        loanSection.setManaged(false);
        cardSection.setVisible(false);
        cardSection.setManaged(false);
        updateDashboardStats();
    }

    @FXML private void showLoans() {
        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        accountSection.setVisible(false);
        accountSection.setManaged(false);
        loanSection.setVisible(true);
        loanSection.setManaged(true);
        cardSection.setVisible(false);
        cardSection.setManaged(false);
        loadLoanRequests();
        dotLoan.setVisible(false);
        updateNotificationCount();
        updateDashboardStats();
    }

    @FXML private void showCards() {
        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        accountSection.setVisible(false);
        accountSection.setManaged(false);
        loanSection.setVisible(false);
        loanSection.setManaged(false);
        cardSection.setVisible(true);
        cardSection.setManaged(true);
        loadCardRequests();
        dotCard.setVisible(false);
        updateNotificationCount();
        updateDashboardStats();
    }

    @FXML private void showAccounts() {
        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        accountSection.setVisible(true);
        accountSection.setManaged(true);
        loanSection.setVisible(false);
        loanSection.setManaged(false);
        cardSection.setVisible(false);
        cardSection.setManaged(false);

        statusFilterCombo.setValue("Pending");
        loadFilteredRequests("Pending");
        dotAccount.setVisible(false);
        updateNotificationCount();
        updateDashboardStats();
    }

    @FXML private void showCustomers() {
        showInfo("Customers", "Coming soon");
    }

    @FXML private void filterByStatus() {
        loadFilteredRequests(statusFilterCombo.getValue());
    }

    private void loadFilteredRequests(String status) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = status.equals("All") ? "SELECT * FROM accounts" : "SELECT * FROM accounts WHERE LOWER(status) = LOWER(?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (!status.equals("All")) stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            requestList.clear();
            while (rs.next()) {
                requestList.add(new AccountRequest(
                        rs.getInt("id"),
                        rs.getString("nominee"),
                        rs.getString("mobile"),
                        rs.getString("account_type"),
                        rs.getString("branch"),
                        rs.getString("status")
                ));
            }

            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("nominee"));
            colMobile.setCellValueFactory(new PropertyValueFactory<>("mobile"));
            colType.setCellValueFactory(new PropertyValueFactory<>("type"));
            colBranch.setCellValueFactory(new PropertyValueFactory<>("branch"));
            accountTable.setItems(requestList);
            addAccountActionButtons();
            updateNotificationCount();
            updateDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Failed to load account requests."); // Changed to use the single-argument showError temporarily
        }
    }

    private void addAccountActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✅");
            private final Button rejectBtn = new Button("❌");
            private final HBox box = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white");
                rejectBtn.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white");

                approveBtn.setOnAction(e -> {
                    AccountRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Approved", "accounts");
                    // No need to setGraphic here, updateStatus will refresh the table.
                });
                rejectBtn.setOnAction(e -> {
                    AccountRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Rejected", "accounts");
                    // No need to setGraphic here, updateStatus will refresh the table.
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                AccountRequest itemData = empty ? null : getTableView().getItems().get(getIndex());

                if (empty || itemData == null) {
                    setGraphic(null);
                } else {
                    if ("Approved".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✔ Approved"));
                    } else if ("Rejected".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✖ Rejected"));
                    } else {
                        setGraphic(box);
                    }
                }
            }
        });
    }

    private void updateStatus(int id, String status, String table) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE " + table + " SET status=? WHERE id=?");
            stmt.setString(1, status);
            stmt.setInt(2, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Fetch username for notification
                String username = getUsernameById(conn, table, id);
                String msg = "";

                if (table.equals("accounts")) {
                    // Update the model object in the ObservableList
                    Optional<AccountRequest> updatedReqOpt = accountTable.getItems().stream()
                            .filter(req -> req.getId() == id)
                            .findFirst();
                    updatedReqOpt.ifPresent(req -> req.setStatus(status)); // setStatus is now available
                    accountTable.refresh(); // Refresh the table to show updated status

                    String accountNumber = "";
                    if (status.equalsIgnoreCase("Approved")) {
                        accountNumber = generateAccountNumber();
                        // Update the account_number in the database if it's an account approval
                        PreparedStatement accNumStmt = null;
                        try {
                            accNumStmt = conn.prepareStatement("UPDATE accounts SET account_number = ? WHERE id = ?");
                            accNumStmt.setString(1, accountNumber);
                            accNumStmt.setInt(2, id);
                            accNumStmt.executeUpdate();
                        } finally {
                            if (accNumStmt != null) accNumStmt.close();
                        }

                        // ✅ Add this code inside the condition where you update an account’s status:
                        if (table.equals("accounts") && "Approved".equals(status)) {
                            // ✅ Set balance equal to deposit for newly approved accounts
                            String sqlBalance = "UPDATE accounts SET balance = deposit WHERE id = ?";
                            PreparedStatement stmtBalance = conn.prepareStatement(sqlBalance);
                            stmtBalance.setInt(1, id);
                            stmtBalance.executeUpdate();
                        }
                        // End of added code

                        msg = "🎉 Your account has been approved! Your new account number is: " + accountNumber;
                    } else {
                        msg = "❌ Your account request was rejected.";
                    }

                } else if (table.equals("loans")) {
                    Optional<LoanRequest> updatedLoanOpt = loanTable.getItems().stream()
                            .filter(loan -> loan.getId() == id)
                            .findFirst();
                    updatedLoanOpt.ifPresent(loan -> loan.setStatus(status));
                    loanTable.refresh();
                    msg = status.equalsIgnoreCase("Approved")
                            ? "✅ Your loan request has been approved!"
                            : "❌ Your loan request was rejected.";
                } else if (table.equals("card_requests")) {
                    Optional<CardRequest> updatedCardOpt = cardTable.getItems().stream()
                            .filter(card -> card.getId() == id)
                            .findFirst();
                    updatedCardOpt.ifPresent(card -> card.setStatus(status)); // setStatus is now available
                    cardTable.refresh();
                    msg = status.equalsIgnoreCase("Approved")
                            ? "✅ Your card request has been approved and is on its way!"
                            : "❌ Your card request was rejected.";
                }

                // Send notification to customer
                if (username != null) {
                    sendNotification(conn, username, msg);
                }

                // Re-load requests to ensure the latest state from DB is reflected, especially for account numbers
                if (table.equals("accounts")) loadFilteredRequests(statusFilterCombo.getValue());
                else if (table.equals("loans")) loadLoanRequests();
                else if (table.equals("card_requests")) loadCardRequests();

                updateNotificationCount();
                updateDashboardStats();
                updateRedDots(); // Ensure red dots are updated after status change
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Status Update Error", "❌ Failed to update status: " + e.getMessage()); // Using the new showError
        }
    }

    // --- NEW HELPER METHODS ---

    // Retrieves the username associated with a request ID from a given table
    private String getUsernameById(Connection conn, String table, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String usernameCol;

        // Determine the username column based on the table
        if (table.equals("accounts")) {
            usernameCol = "username"; // Assuming 'accounts' table also has a 'username' column
        } else if (table.equals("loans") || table.equals("card_requests")) {
            usernameCol = "username";
        } else {
            return null; // Or throw an IllegalArgumentException
        }

        try {
            stmt = conn.prepareStatement("SELECT " + usernameCol + " FROM " + table + " WHERE id=?");
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(usernameCol) : null;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }
    }

    // Sends a notification to a specific user
    private void sendNotification(Connection conn, String username, String message) throws SQLException {
        PreparedStatement stmt = null;
        try {
            // Ensure your 'notifications' table has 'username', 'message', 'created_at' (TIMESTAMP DEFAULT CURRENT_TIMESTAMP), 'is_read' (BOOLEAN DEFAULT FALSE)
            stmt = conn.prepareStatement("INSERT INTO notifications (username, message, is_read) VALUES (?, ?, FALSE)");
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    // Generates a unique 12-digit account number
    private String generateAccountNumber() {
        Random random = new Random();
        // Generate a 12-digit number (between 10^11 and 10^12 - 1)
        long num = 100_000_000_000L + (long)(random.nextDouble() * 900_000_000_000L);
        return String.valueOf(num);
    }

    // --- END NEW HELPER METHODS ---


    private void loadLoanRequests() {
        ObservableList<LoanRequest> loanList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnector.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT * FROM loans").executeQuery();
            while (rs.next()) {
                loanList.add(new LoanRequest(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("loan_type"),
                        rs.getDouble("amount"),
                        rs.getInt("duration"),
                        rs.getString("reason"),
                        rs.getString("status")
                ));
            }
            loanIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            loanUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
            loanTypeCol.setCellValueFactory(new PropertyValueFactory<>("loanType"));
            loanAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            loanDurationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
            loanReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
            loanStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            loanTable.setItems(loanList);
            addLoanActionButtons();
            updateNotificationCount();
            updateDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Loan Request Error", "❌ Failed to load loan requests: " + e.getMessage()); // Using the new showError
        }
    }

    private void addLoanActionButtons() {
        loanActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✅");
            private final Button rejectBtn = new Button("❌");
            private final HBox box = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white");
                rejectBtn.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white");

                approveBtn.setOnAction(e -> {
                    LoanRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Approved", "loans");
                });
                rejectBtn.setOnAction(e -> {
                    LoanRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Rejected", "loans");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                LoanRequest itemData = empty ? null : getTableView().getItems().get(getIndex());

                if (empty || itemData == null) {
                    setGraphic(null);
                } else {
                    if ("Approved".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✔ Approved"));
                    } else if ("Rejected".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✖ Rejected"));
                    } else {
                        setGraphic(box);
                    }
                }
            }
        });
    }

    private void loadCardRequests() {
        ObservableList<CardRequest> cardList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnector.getConnection()) {
            ResultSet rs = conn.prepareStatement("SELECT * FROM card_requests").executeQuery();
            while (rs.next()) {
                cardList.add(new CardRequest(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("card_type"),
                        rs.getString("mobile"),
                        rs.getString("delivery_address"),
                        rs.getString("status")
                ));
            }
            cardIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            cardUserCol.setCellValueFactory(new PropertyValueFactory<>("username"));
            cardTypeCol.setCellValueFactory(new PropertyValueFactory<>("cardType"));
            cardMobileCol.setCellValueFactory(new PropertyValueFactory<>("mobile"));
            cardAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
            cardStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
            cardTable.setItems(cardList);
            addCardActionButtons();
            updateNotificationCount();
            updateDashboardStats();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Card Request Error", "❌ Failed to load card requests: " + e.getMessage()); // Using the new showError
        }
    }

    private void addCardActionButtons() {
        cardActionCol.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("✅");
            private final Button rejectBtn = new Button("❌");
            private final HBox box = new HBox(10, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white");
                rejectBtn.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white");

                approveBtn.setOnAction(e -> {
                    CardRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Approved", "card_requests");
                });
                rejectBtn.setOnAction(e -> {
                    CardRequest req = getTableView().getItems().get(getIndex());
                    updateStatus(req.getId(), "Rejected", "card_requests");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                CardRequest itemData = empty ? null : getTableView().getItems().get(getIndex());

                if (empty || itemData == null) {
                    setGraphic(null);
                } else {
                    if ("Approved".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✔ Approved"));
                    } else if ("Rejected".equalsIgnoreCase(itemData.getStatus())) {
                        setGraphic(new Label("✖ Rejected"));
                    } else {
                        setGraphic(box);
                    }
                }
            }
        });
    }

    // This method is no longer used and can be removed or kept as a reference
    /*
    private HBox createActionButtons(Runnable approve, Runnable reject) {
        Button a = new Button("✅");
        Button r = new Button("❌");
        a.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white");
        r.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white");
        a.setOnAction(e -> approve.run());
        r.setOnAction(e -> reject.run());
        return new HBox(10, a, r);
    }
    */

    private void updateNotificationCount() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            int pending1 = getPendingCount(conn, "accounts");
            int pending2 = getPendingCount(conn, "loans");
            int pending3 = getPendingCount(conn, "card_requests");
            notificationBadge.setText("🔔 " + (pending1 + pending2 + pending3));
        } catch (Exception e) {
            notificationBadge.setText("🔔 ?");
            e.printStackTrace(); // Added for debugging
        }
    }

    private void updateDashboardStats() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            int pendingAccounts = getPendingCount(conn, "accounts");
            int pendingLoans = getPendingCount(conn, "loans");
            int pendingCards = getPendingCount(conn, "card_requests");

            accStat.setText(String.valueOf(pendingAccounts));
            loanStat.setText(String.valueOf(pendingLoans));
            cardStat.setText(String.valueOf(pendingCards));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>("Accounts", pendingAccounts));
            series.getData().add(new XYChart.Data<>("Loans", pendingLoans));
            series.getData().add(new XYChart.Data<>("Cards", pendingCards));

            barChart.getData().clear();
            barChart.getData().add(series);

            updateRedDots();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Dashboard Stats Error", "❌ Failed to update dashboard statistics: " + e.getMessage()); // Using the new showError
        }
    }

    private int getPendingCount(Connection conn, String table) {
        try {
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE status='Pending'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateRedDots() {
        dotAccount.setVisible(getPendingCount("accounts") > 0);
        dotLoan.setVisible(getPendingCount("loans") > 0);
        dotCard.setVisible(getPendingCount("card_requests") > 0);
    }

    private int getPendingCount(String table) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE status='Pending'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @FXML private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            // Keeping this line as per your preference, though it's typically 'Stage stage = (Stage) accountTable.getScene().getWindow(); stage.setScene(new Scene(root));'
            accountTable.getScene().setRoot(root);
        } catch (IOException e) { // Changed to IOException as FXMLLoader.load throws it
            System.err.println("Error loading login.fxml: " + e.getMessage());
            e.printStackTrace();
            showError("Logout Error", "❌ Could not load login screen."); // Added a more specific error using the new method
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // Re-added this method as requested
    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Renamed existing showError to handle single string message for backward compatibility or direct use if needed.
    // However, it's generally better to use the more specific two-argument version.
    private void showError(String msg) {
        showError("Error", msg); // Delegate to the two-argument method
    }
}