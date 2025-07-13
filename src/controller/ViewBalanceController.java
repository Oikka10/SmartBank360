package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ViewBalanceController {

    @FXML private Label nameLabel;
    @FXML private Label accountLabel;
    @FXML private Label balanceLabel;

    private String currentUsername;

    public void setUsername(String username) {
        this.currentUsername = username;
        loadBalance();
    }

    private void loadBalance() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT nominee, account_number, balance FROM accounts WHERE username = ? AND status = 'Approved' ORDER BY id DESC LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameLabel.setText(rs.getString("nominee"));
                accountLabel.setText(rs.getString("account_number"));
                balanceLabel.setText(String.format("%.2f", rs.getDouble("balance")));
            } else {
                nameLabel.setText("Not found");
                accountLabel.setText("Not found");
                balanceLabel.setText("৳0.00");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
        stage.close();
    }
}
