package vowelcapt.utils;

public class Account {

    private String userName;
    private String password;
    private String gender;

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

    @Override
    public String toString() {
        return userName + " " + password + " " + gender;
    }
}
