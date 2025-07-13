package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CardRequestController {

    @FXML private ComboBox<String> cardTypeBox;
    @FXML private TextField deliveryField, mobileField, accountIdField;
    @FXML private Label messageLabel;
    @FXML private Button submitBtn;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    @FXML
    public void initialize() {
        cardTypeBox.getItems().addAll("Debit", "Credit", "Prepaid");
        submitBtn.setOnAction(e -> handleCardRequest());
    }

    private void handleCardRequest() {
        String type = cardTypeBox.getValue();
        String address = deliveryField.getText();
        String mobile = mobileField.getText();
        String accId = accountIdField.getText();

        if (type == null || address.isEmpty() || mobile.isEmpty() || accId.isEmpty()) {
            messageLabel.setText("⚠️ Please fill all fields.");
            return;
        }

        try {
            Connection conn = DatabaseConnector.getConnection();
            String sql = "INSERT INTO card_requests (username, card_type, delivery_address, mobile, account_id, status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, type);
            stmt.setString(3, address);
            stmt.setString(4, mobile);
            stmt.setInt(5, Integer.parseInt(accId));
            stmt.setString(6, "Pending");

            stmt.executeUpdate();
            messageLabel.setText("✅ Card request submitted!");

            clearForm();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("❌ Error while submitting request.");
        }
    }

    private void clearForm() {
        cardTypeBox.setValue(null);
        deliveryField.clear();
        mobileField.clear();
        accountIdField.clear();
    }
}
