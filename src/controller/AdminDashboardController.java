package controller;

// Core JavaFX imports
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

// Specific JavaFX controls and collections for Officer management
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory; // Still needed for Customer class

// File I/O imports for saving/loading officers
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.IOException;

// Imports for scene switching (for logout functionality)
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class AdminDashboardController {

    @FXML private Label adminNameLabel;
    @FXML private Label totalBalanceLabel;
    @FXML private Label incomeLabel;
    @FXML private Label expenseLabel;
    @FXML private ListView<String> recentList;

    @FXML private VBox dashboardView;
    @FXML private VBox officerView;
    @FXML private VBox customerView;
    @FXML private VBox requestView;

    // FXML fields for Officer management (NOW INCLUDING DEPARTMENT)
    @FXML private TextField officerNameField, officerEmailField, officerDepartmentField; // officerDepartmentField added
    @FXML private PasswordField officerPasswordField;
    @FXML private TableView<Officer> officerTable;
    @FXML private TableColumn<Officer, String> nameCol, emailCol, passwordCol, departmentCol; // departmentCol added

    // FXML fields for Customer management
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> custNameCol;
    @FXML private TableColumn<Customer, String> custAccCol;
    @FXML private TableColumn<Customer, String> custBalanceCol;

    // FXML field for Request management
    @FXML private ListView<String> requestList;

    // ObservableList to hold officer data
    private final ObservableList<Officer> officerList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        adminNameLabel.setText("Welcome, Admin");

        // Set example values (you can later replace with DB values)
        totalBalanceLabel.setText("82300");
        incomeLabel.setText("5200");
        expenseLabel.setText("2800");

        recentList.getItems().addAll(
                "Account request from Tanvir",
                "Loan request from Sumaiya",
                "New Officer added"
        );

        // Default view
        showDashboard();

        // Setup the Officer TableView columns (Using .property() methods for SimpleStringProperty)
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        passwordCol.setCellValueFactory(cellData -> cellData.getValue().passwordProperty());
        departmentCol.setCellValueFactory(cellData -> cellData.getValue().departmentProperty()); // Department column setup

        // Load existing officers from file when the application starts
        loadOfficersFromFile();


        // Setup the Customer TableView columns (PropertyValueFactory is fine for plain String properties)
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        custAccCol.setCellValueFactory(new PropertyValueFactory<>("account"));
        custBalanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Add sample data to the Customer TableView
        customerTable.getItems().addAll(
                new Customer("Bithi", "ACC123", "18,000"),
                new Customer("Reshma", "ACC456", "9,500"),
                new Customer("Mahedi", "ACC789", "32,400")
        );

        // Add sample data to the Request ListView
        requestList.getItems().addAll(
                "📥 Account request from Rafi",
                "📥 Card reissue request from Tonmoy",
                "📥 Loan request from Sohana"
        );
    }

    private void showOnly(VBox selected) {
        dashboardView.setVisible(false); dashboardView.setManaged(false);
        officerView.setVisible(false);   officerView.setManaged(false);
        customerView.setVisible(false);  customerView.setManaged(false);
        requestView.setVisible(false);   requestView.setManaged(false);

        selected.setVisible(true);
        selected.setManaged(true);
    }

    @FXML private void showDashboard() {
        showOnly(dashboardView);
    }

    @FXML private void showOfficers() {
        showOnly(officerView);
    }

    @FXML private void showCustomers() {
        showOnly(customerView);
    }

    @FXML private void showRequests() {
        showOnly(requestView);
    }

    // handleLogout() method to switch to login.fxml
    @FXML
    private void handleLogout() {
        try {
            // Load the login FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent loginRoot = loader.load();

            // Get the current stage using any FXML element's scene
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();

            // Create a new scene with the login root
            Scene scene = new Scene(loginRoot);

            // Set the new scene on the stage
            stage.setScene(scene);
            stage.centerOnScreen(); // Optional: center the new scene
            stage.setTitle("SmartBank360 - Login"); // Set the title for the login window
        } catch (IOException e) {
            System.err.println("Error loading login.fxml: " + e.getMessage());
            e.printStackTrace();
            // Optionally, show an alert to the user if the login screen can't be loaded
            // Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load login screen.");
            // alert.showAndWait();
        }
    }

    // Officer class (UPDATED to include Department)
    public static class Officer {
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty password;
        private final SimpleStringProperty department; // NEW: Department field

        public Officer(String name, String email, String password, String department) { // Constructor updated
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.password = new SimpleStringProperty(password);
            this.department = new SimpleStringProperty(department);
        }

        // Getters for TableColumn (used by PropertyValueFactory, and for file I/O)
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getPassword() { return password.get(); }
        public String getDepartment() { return department.get(); } // NEW: Department getter

        // Property methods for TableColumn lambda expressions
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty passwordProperty() { return password; }
        public SimpleStringProperty departmentProperty() { return department; } // NEW: Department property
    }

    // Customer class
    public static class Customer {
        private final String name;
        private final String account;
        private final String balance;

        public Customer(String name, String account, String balance) {
            this.name = name;
            this.account = account;
            this.balance = balance;
        }

        public String getName() { return name; }
        public String getAccount() { return account; }
        public String getBalance() { return balance; }
    }

    // Button Logic for adding an officer (UPDATED to include Department)
    @FXML
    private void addOfficer() {
        String name = officerNameField.getText().trim();
        String email = officerEmailField.getText().trim();
        String password = officerPasswordField.getText().trim();
        String department = officerDepartmentField.getText().trim(); // Get department text

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !department.isEmpty()) { // Validate department
            Officer officer = new Officer(name, email, password, department); // Pass department to constructor
            officerList.add(officer);
            officerTable.setItems(officerList); // Ensure the table's items are updated

            saveOfficersToFile(); // Save to file for persistence

            // Clear all input fields after adding
            officerNameField.clear();
            officerEmailField.clear();
            officerPasswordField.clear();
            officerDepartmentField.clear(); // Clear department field
        } else {
            System.out.println("⚠️ Please fill in all officer fields.");
        }
    }

    // Method to delete selected request from the ListView
    @FXML
    private void deleteSelectedRequest() {
        String selected = requestList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            requestList.getItems().remove(selected);
            System.out.println("✅ Deleted: " + selected);
        } else {
            System.out.println("⚠️ No request selected.");
        }
    }

    // === Save to File === (UPDATED for Department)
    private void saveOfficersToFile() {
        try (PrintWriter writer = new PrintWriter("officers.txt")) {
            for (Officer o : officerList) {
                // Include department in the saved line, separated by a comma
                writer.println(o.getName() + "," + o.getEmail() + "," + o.getPassword() + "," + o.getDepartment());
            }
        } catch (IOException e) {
            System.err.println("Error saving officers to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === Load from File === (UPDATED for Department)
    private void loadOfficersFromFile() {
        File file = new File("officers.txt");
        if (!file.exists()) {
            System.out.println("No officers.txt found. Starting with empty officer list.");
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 4) { // Now expect 4 parts (Name, Email, Password, Department)
                    officerList.add(new Officer(parts[0], parts[1], parts[2], parts[3])); // Pass department
                } else {
                    System.err.println("Skipping malformed officer record (expected 4 parts): " + String.join(",", parts));
                }
            }
            officerTable.setItems(officerList); // Set items to the table after loading
        } catch (IOException e) {
            System.err.println("Error loading officers from file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}