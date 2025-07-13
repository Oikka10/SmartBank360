package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        transactionTypeBox.getItems().addAll("Send Money", "Mobile Recharge", "Credit Card Payment");
        transactionTypeBox.setValue("Send Money");

        sendBtn.setOnAction(e -> processTransaction());
    }

    private void processTransaction() {
        String type = transactionTypeBox.getValue();
        String to = recipientField.getText();
        String amountText = amountField.getText();

        if (to.isEmpty() || amountText.isEmpty()) {
            statusLabel.setText("⚠️ Please fill all fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                statusLabel.setText("❌ Invalid amount.");
                return;
            }

            try (Connection conn = DatabaseConnector.getConnection()) {
                String sql = "INSERT INTO transactions (username, type, recipient, amount, datetime) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, currentUsername);
                stmt.setString(2, type);
                stmt.setString(3, to);
                stmt.setDouble(4, amount);

                String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                stmt.setString(5, time);

                stmt.executeUpdate();

                statusLabel.setText("✅ Transaction successful!");
                recipientField.clear();
                amountField.clear();

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Error during transaction.");
            }

        } catch (NumberFormatException ex) {
            statusLabel.setText("❌ Amount must be numeric.");
        }
    }
}
