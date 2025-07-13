package model;

import javafx.beans.property.*;

public class Transaction {
    private final StringProperty datetime;
    private final StringProperty type;
    private final StringProperty recipient;
    private final DoubleProperty amount;

    public Transaction(String datetime, String type, String recipient, double amount) {
        this.datetime = new SimpleStringProperty(datetime);
        this.type = new SimpleStringProperty(type);
        this.recipient = new SimpleStringProperty(recipient);
        this.amount = new SimpleDoubleProperty(amount);
    }

    public StringProperty datetimeProperty() { return datetime; }
    public StringProperty typeProperty() { return type; }
    public StringProperty recipientProperty() { return recipient; }
    public DoubleProperty amountProperty() { return amount; }
}
