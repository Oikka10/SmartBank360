package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Transaction;
import utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TransactionHistoryController {

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colRecipient;
    @FXML private TableColumn<Transaction, Double> colAmount;
    @FXML private Label emptyLabel;

    private String currentUsername;

    public void setUsername(String username) {
        this.currentUsername = username;
        loadTransactions();
    }

    private void loadTransactions() {
        ObservableList<Transaction> list = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.getConnection()) {
            String sql = "SELECT * FROM transactions WHERE username = ? ORDER BY datetime DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUsername);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getString("datetime"),
                        rs.getString("type"),
                        rs.getString("recipient"),
                        rs.getDouble("amount")
                ));
            }

            colDate.setCellValueFactory(data -> data.getValue().datetimeProperty());
            colType.setCellValueFactory(data -> data.getValue().typeProperty());
            colRecipient.setCellValueFactory(data -> data.getValue().recipientProperty());
            colAmount.setCellValueFactory(data -> data.getValue().amountProperty().asObject());

            transactionTable.setItems(list);
            emptyLabel.setVisible(list.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
