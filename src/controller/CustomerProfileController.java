package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerProfileController {

    @FXML private TextField nameField, mobileField, nidField, addressField, emailField;
    @FXML private Label statusLabel;

    private String username;

    public void setUsername(String username) {
        this.username = username;
        loadProfile();
    }

    private void loadProfile() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT nominee, mobile, nid, address, email FROM accounts WHERE username = ? ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nameField.setText(rs.getString("nominee"));
                mobileField.setText(rs.getString("mobile"));
                nidField.setText(rs.getString("nid"));
                addressField.setText(rs.getString("address"));
                emailField.setText(rs.getString("email"));
                setFieldsEditable(false);
            } else {
                statusLabel.setText("⚠️ No account found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error loading profile.");
        }
    }

    @FXML
    private void enableEdit() {
        setFieldsEditable(true);
    }

    @FXML
    private void saveChanges() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "UPDATE accounts SET nominee=?, mobile=?, nid=?, address=?, email=? WHERE username=?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nameField.getText());
            stmt.setString(2, mobileField.getText());
            stmt.setString(3, nidField.getText());
            stmt.setString(4, addressField.getText());
            stmt.setString(5, emailField.getText());
            stmt.setString(6, username);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                statusLabel.setText("✅ Profile updated successfully!");
                setFieldsEditable(false);
            } else {
                statusLabel.setText("⚠️ Update failed. Try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error while saving.");
        }
    }

    private void setFieldsEditable(boolean editable) {
        nameField.setEditable(editable);
        mobileField.setEditable(editable);
        nidField.setEditable(editable);
        addressField.setEditable(editable);
        emailField.setEditable(editable);
    }
}
