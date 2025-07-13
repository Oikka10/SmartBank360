package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

import java.sql.*;

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
        loadFilteredRequests(statusFilterCombo.getValue());
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
                        rs.getString("branch")
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
        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Failed to load account requests.");
        }
    }

    private void addAccountActionButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final HBox box = createActionButtons(
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Approved", "accounts"),
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Rejected", "accounts")
            );

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void updateStatus(int id, String status, String table) {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE " + table + " SET status=? WHERE id=?");
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();

            if (table.equals("accounts")) loadFilteredRequests(statusFilterCombo.getValue());
            else if (table.equals("loans")) loadLoanRequests();
            else if (table.equals("card_requests")) loadCardRequests();
        } catch (Exception e) {
            e.printStackTrace();
            showError("❌ Failed to update status.");
        }
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLoanActionButtons() {
        loanActionCol.setCellFactory(param -> new TableCell<>() {
            private final HBox box = createActionButtons(
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Approved", "loans"),
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Rejected", "loans")
            );
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addCardActionButtons() {
        cardActionCol.setCellFactory(param -> new TableCell<>() {
            private final HBox box = createActionButtons(
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Approved", "card_requests"),
                    () -> updateStatus(getTableView().getItems().get(getIndex()).getId(), "Rejected", "card_requests")
            );
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private HBox createActionButtons(Runnable approve, Runnable reject) {
        Button a = new Button("✅");
        Button r = new Button("❌");
        a.setStyle("-fx-background-color:#27ae60;-fx-text-fill:white");
        r.setStyle("-fx-background-color:#c0392b;-fx-text-fill:white");
        a.setOnAction(e -> approve.run());
        r.setOnAction(e -> reject.run());
        return new HBox(10, a, r);
    }

    private void updateNotificationCount() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            int pending1 = count(conn, "accounts");
            int pending2 = count(conn, "loans");
            int pending3 = count(conn, "card_requests");
            notificationBadge.setText("🔔 " + (pending1 + pending2 + pending3));
        } catch (Exception e) {
            notificationBadge.setText("🔔 ?");
        }
    }

    private int count(Connection conn, String table) throws SQLException {
        ResultSet rs = conn.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE status='Pending'").executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    private void updateDashboardStats() {
        accStat.setText(String.valueOf(accountTable.getItems().size()));
        loanStat.setText(String.valueOf(loanTable.getItems().size()));
        cardStat.setText(String.valueOf(cardTable.getItems().size()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Accounts", accountTable.getItems().size()));
        series.getData().add(new XYChart.Data<>("Loans", loanTable.getItems().size()));
        series.getData().add(new XYChart.Data<>("Cards", cardTable.getItems().size()));

        barChart.getData().clear();
        barChart.getData().add(series);
    }

    @FXML private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            accountTable.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("❌ Error");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
