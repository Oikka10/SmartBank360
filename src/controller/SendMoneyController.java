package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import db.DatabaseConnection;   // ✅ Use your db connection class
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SendMoneyController {

    @FXML private ComboBox<String> transactionTypeBox;
    @FXML private TextField recipientField;
    @FXML private TextField amountField;
    @FXML private Button sendBtn;
    @FXML private Label statusLabel;

    private String currentUsername;

    public void setUsername(String username) {
        this.currentUsername = username;
    }

    @FXML
    public void initialize() {
        transactionTypeBox.getItems().addAll("Transfer Money"); // ✅ only bank transfers
        transactionTypeBox.setValue("Transfer Money");
        sendBtn.setOnAction(e -> processTransaction());
    }

    private void processTransaction() {
        String toAccount = recipientField.getText().trim();
        String amountText = amountField.getText().trim();

        if (toAccount.isEmpty() || amountText.isEmpty()) {
            statusLabel.setText("⚠️ Please fill all fields.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                statusLabel.setText("❌ Enter a valid amount.");
                return;
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("❌ Amount must be numeric.");
            return;
        }

        Connection conn = DatabaseConnection.connect();  // ✅ FIXED connection
        if (conn == null) {
            statusLabel.setText("❌ Database connection failed.");
            return;
        }

        try {
            conn.setAutoCommit(false);

            // ✅ Check receiver exists
            PreparedStatement checkReceiver = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_number=? AND status='Approved' LIMIT 1");
            checkReceiver.setString(1, toAccount);
            ResultSet rsReceiver = checkReceiver.executeQuery();
            if (!rsReceiver.next()) {
                statusLabel.setText("❌ Receiver account not found or not approved.");
                conn.rollback();
                return;
            }

            // ✅ Get sender account
            PreparedStatement checkSender = conn.prepareStatement(
                    "SELECT account_number, balance FROM accounts WHERE username=? AND status='Approved' ORDER BY id DESC LIMIT 1");
            checkSender.setString(1, currentUsername);
            ResultSet rsSender = checkSender.executeQuery();
            if (!rsSender.next()) {
                statusLabel.setText("❌ Sender account not found.");
                conn.rollback();
                return;
            }

            String senderAcc = rsSender.getString("account_number");
            double senderBalance = rsSender.getDouble("balance");

            if (senderAcc.equals(toAccount)) {
                statusLabel.setText("⚠️ Cannot transfer to the same account.");
                conn.rollback();
                return;
            }
            if (senderBalance < amount) {
                statusLabel.setText("⚠️ Insufficient funds.");
                conn.rollback();
                return;
            }

            // ✅ Deduct from sender
            PreparedStatement debit = conn.prepareStatement(
                    "UPDATE accounts SET balance=balance-? WHERE account_number=?");
            debit.setDouble(1, amount);
            debit.setString(2, senderAcc);
            debit.executeUpdate();

            // ✅ Credit to receiver
            PreparedStatement credit = conn.prepareStatement(
                    "UPDATE accounts SET balance=balance+? WHERE account_number=?");
            credit.setDouble(1, amount);
            credit.setString(2, toAccount);
            credit.executeUpdate();

            // ✅ Insert sender transaction
            PreparedStatement insertSender = conn.prepareStatement(
                    "INSERT INTO transactions (username, type, recipient, amount, datetime) VALUES (?, 'Debit', ?, ?, NOW())");
            insertSender.setString(1, currentUsername);
            insertSender.setString(2, toAccount);
            insertSender.setDouble(3, amount);
            insertSender.executeUpdate();

            // ✅ Insert receiver transaction
            PreparedStatement insertReceiver = conn.prepareStatement(
                    "INSERT INTO transactions (username, type, recipient, amount, datetime) VALUES (?, 'Credit', ?, ?, NOW())");
            insertReceiver.setString(1, toAccount);
            insertReceiver.setString(2, senderAcc);
            insertReceiver.setDouble(3, amount);
            insertReceiver.executeUpdate();

            // ✅ Receiver notification
            PreparedStatement notifyReceiver = conn.prepareStatement(
                    "INSERT INTO notifications (username, message, is_read, created_at) VALUES (?, ?, 0, NOW())");
            notifyReceiver.setString(1, toAccount);
            notifyReceiver.setString(2, "💰 " + amount + " BDT credited to your account from " + senderAcc);
            notifyReceiver.executeUpdate();

            // ✅ Sender notification
            PreparedStatement notifySender = conn.prepareStatement(
                    "INSERT INTO notifications (username, message, is_read, created_at) VALUES (?, ?, 0, NOW())");
            notifySender.setString(1, currentUsername);
            notifySender.setString(2, "📤 " + amount + " BDT debited from your account to " + toAccount);
            notifySender.executeUpdate();

            conn.commit();

            statusLabel.setText("✅ Transfer successful!");
            recipientField.clear();
            amountField.clear();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            statusLabel.setText("❌ Transaction failed.");
        } finally {
            try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
        }
    }
}
