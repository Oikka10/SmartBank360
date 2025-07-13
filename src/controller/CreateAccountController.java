package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class CreateAccountController {

    @FXML private ComboBox<String> accountTypeBox;
    @FXML private ComboBox<String> branchBox;
    @FXML private ComboBox<String> currencyBox;

    @FXML private TextField depositField;
    @FXML private CheckBox cardCheck;

    @FXML private TextField nomineeField;
    @FXML private TextField mobileField;
    @FXML private TextField nidField;
    @FXML private TextField addressField;
    @FXML private TextField emailField;

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Button submitBtn;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        accountTypeBox.getItems().addAll("Savings", "Current", "Student");
        branchBox.getItems().addAll("Dhaka", "Chittagong", "Rajshahi", "Sylhet");
        currencyBox.getItems().addAll("BDT", "USD", "EUR");

        submitBtn.setOnAction(e -> handleAccountCreation());
    }

    private void handleAccountCreation() {
        String accType = accountTypeBox.getValue();
        String branch = branchBox.getValue();
        String depositText = depositField.getText();
        String currency = currencyBox.getValue();

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
            messageLabel.setText("⚠️ Please fill in all fields!");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("❌ Passwords do not match!");
            return;
        }

        try {
            double deposit = Double.parseDouble(depositText);
            if (deposit < 1000) {
                messageLabel.setText("⚠️ Minimum deposit is ৳1000!");
                return;
            }

            String accountNumber = generateAccountNumber();

            Connection conn = DatabaseConnector.getConnection();
            String sql = "INSERT INTO accounts (account_type, branch, currency, deposit, card_needed, nominee, mobile, nid, address, email, password, account_number, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
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

            stmt.executeUpdate();
            messageLabel.setText("✅ Account request submitted!");

            clearForm();

        } catch (NumberFormatException ex) {
            messageLabel.setText("❌ Invalid deposit amount.");
        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("❌ Error occurred while saving.");
        }
    }

    private String generateAccountNumber() {
        Random rand = new Random();
        long number = 100000000000L + (long)(rand.nextDouble() * 899999999999L);
        return String.valueOf(number);
    }

    private void clearForm() {
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
    }
}
