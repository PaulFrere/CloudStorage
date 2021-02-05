package messages;

public class RegisterUserMsg extends AbstractMsg {
    private String login;
    private String pass;
    private String nick;

    public RegisterUserMsg(String login, String password, String nickname) {
        this.login = login;
        this.pass = pass;
        this.nick = nick;
    }

    public String getUsername() {
        return login;
    }

    public String getPassword() {
        return pass;
    }

    public String getNickname() {
        return nick;
    }
}