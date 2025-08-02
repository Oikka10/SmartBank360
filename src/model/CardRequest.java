package model;

public class CardRequest {
    private int id;
    private String username, cardType, mobile, address, status; // NEW: Added status field

    public CardRequest(int id, String username, String cardType, String mobile, String address, String status) { // UPDATED Constructor
        this.id = id;
        this.username = username;
        this.cardType = cardType;
        this.mobile = mobile;
        this.address = address;
        this.status = status; // Initialize status
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCardType() { return cardType; }
    public String getMobile() { return mobile; }
    public String getAddress() { return address; }
    public String getStatus() { return status; } // Getter for status

    public void setStatus(String status) { this.status = status; } // NEW: Setter for status
}