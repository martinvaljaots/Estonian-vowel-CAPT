package vowelcapt.utils;

public class Account {

    private String userName;
    private String password;
    private String gender;
    private double threshold;

    public Account(String userName, String password, String gender) {
        this.userName = userName;
        this.password = password;
        this.gender = gender;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String toString() {
        return userName + " " + password + " " + gender + " " + threshold;
    }
}
