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

    // ✅ Property Methods
    public StringProperty datetimeProperty() { return datetime; }
    public StringProperty typeProperty() { return type; }
    public StringProperty recipientProperty() { return recipient; }
    public DoubleProperty amountProperty() { return amount; }

    // ✅ Normal Getter Methods
    public String getDatetime() { return datetime.get(); }
    public String getType() { return type.get(); }
    public String getRecipient() { return recipient.get(); }
    public double getAmount() { return amount.get(); }

    // ✅ Normal Setter Methods (optional but useful)
    public void setDatetime(String value) { datetime.set(value); }
    public void setType(String value) { type.set(value); }
    public void setRecipient(String value) { recipient.set(value); }
    public void setAmount(double value) { amount.set(value); }
}
