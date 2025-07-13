package model;

public class CardRequest {
    private int id;
    private String username, cardType, mobile, address, status;

    public CardRequest(int id, String username, String cardType, String mobile, String address, String status) {
        this.id = id;
        this.username = username;
        this.cardType = cardType;
        this.mobile = mobile;
        this.address = address;
        this.status = status;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCardType() { return cardType; }
    public String getMobile() { return mobile; }
    public String getAddress() { return address; }
    public String getStatus() { return status; }
}
