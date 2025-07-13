package model;

public class LoanRequest {
    private int id;
    private String username, loanType, reason, status;
    private double amount;
    private int duration;

    public LoanRequest(int id, String username, String loanType, double amount, int duration, String reason, String status) {
        this.id = id;
        this.username = username;
        this.loanType = loanType;
        this.amount = amount;
        this.duration = duration;
        this.reason = reason;
        this.status = status;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getLoanType() { return loanType; }
    public double getAmount() { return amount; }
    public int getDuration() { return duration; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LoanRequest{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", loanType='" + loanType + '\'' +
                ", amount=" + amount +
                ", duration=" + duration +
                ", reason='" + reason + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
