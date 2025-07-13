package model;

public class AccountRequest {
    private int id;
    private String nominee, mobile, type, branch;

    public AccountRequest(int id, String nominee, String mobile, String type, String branch) {
        this.id = id;
        this.nominee = nominee;
        this.mobile = mobile;
        this.type = type;
        this.branch = branch;
    }

    public int getId() { return id; }
    public String getNominee() { return nominee; }
    public String getMobile() { return mobile; }
    public String getType() { return type; }
    public String getBranch() { return branch; }

    @Override
    public String toString() {
        return "AccountRequest{" +
                "id=" + id +
                ", nominee='" + nominee + '\'' +
                ", mobile='" + mobile + '\'' +
                ", type='" + type + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}
