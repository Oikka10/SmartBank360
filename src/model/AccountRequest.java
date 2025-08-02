package model;

public class AccountRequest {
    private int id;
    private String nominee, mobile, type, branch;
    private String status; // NEW: Added status field

    public AccountRequest(int id, String nominee, String mobile, String type, String branch, String status) { // UPDATED Constructor
        this.id = id;
        this.nominee = nominee;
        this.mobile = mobile;
        this.type = type;
        this.branch = branch;
        this.status = status; // Initialize status
    }

    public int getId() { return id; }
    public String getNominee() { return nominee; }
    public String getMobile() { return mobile; }
    public String getType() { return type; }
    public String getBranch() { return branch; }
    public String getStatus() { return status; } // NEW: Getter for status

    public void setStatus(String status) { this.status = status; } // NEW: Setter for status

    @Override
    public String toString() {
        return "AccountRequest{" +
                "id=" + id +
                ", nominee='" + nominee + '\'' +
                ", mobile='" + mobile + '\'' +
                ", type='" + type + '\'' +
                ", branch='" + branch + '\'' +
                ", status='" + status + '\'' + // Include status in toString
                '}';
    }
}