package vowelcapt.utils;

public class Account {

    private String userName;
    private String password;
    private String sex;

    public Account(String userName, String password, String sex) {
        this.userName = userName;
        this.password = password;
        this.sex = sex;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getSex() {
        return sex;
    }

    @Override
    public String toString() {
        return userName + " " + password + " " + sex;
    }
}
