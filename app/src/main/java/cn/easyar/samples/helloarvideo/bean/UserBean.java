package cn.easyar.samples.helloarvideo.bean;

/**
 * Created by 毛奇志 on 2018/3/18.
 */

public class UserBean {
    private int id;
    private String username;
    private String password;

    public UserBean(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public UserBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
