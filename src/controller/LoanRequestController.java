package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoanRequestController {

    @FXML private ComboBox<String> loanTypeBox;
    @FXML private TextField amountField, durationField, reasonField, accountIdField;
    @FXML private Button submitBtn;
    @FXML private Label messageLabel;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        loanTypeBox.getItems().addAll("Personal", "Education", "Business", "Car", "Home");
        submitBtn.setOnAction(e -> handleLoanRequest());
    }

    private void handleLoanRequest() {
        String type = loanTypeBox.getValue();
        String amount = amountField.getText();
        String duration = durationField.getText();
        String reason = reasonField.getText();
        String accountId = accountIdField.getText();

        if (type == null || amount.isEmpty() || duration.isEmpty() || reason.isEmpty() || accountId.isEmpty()) {
            messageLabel.setText("⚠️ Please fill all fields.");
            return;
        }

        try {
            double loanAmt = Double.parseDouble(amount);
            int months = Integer.parseInt(duration);
            int accId = Integer.parseInt(accountId);

            Connection conn = DatabaseConnector.getConnection();
            String sql = "INSERT INTO loan_requests (username, loan_type, amount, duration, reason, account_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, type);
            stmt.setDouble(3, loanAmt);
            stmt.setInt(4, months);
            stmt.setString(5, reason);
            stmt.setInt(6, accId);
            stmt.setString(7, "Pending");

            stmt.executeUpdate();
            messageLabel.setText("✅ Loan request submitted!");
            clearForm();

        } catch (NumberFormatException ex) {
            messageLabel.setText("❌ Invalid number input.");
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("❌ Error submitting request.");
        }
    }

    private void clearForm() {
        loanTypeBox.setValue(null);
        amountField.clear();
        durationField.clear();
        reasonField.clear();
        accountIdField.clear();
    }
}
