package messages;

public class AuthMsg extends AbstractMsg {
    private String login;
    private String pass;
    private String nick;

    public AuthMsg(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public AuthMsg(String nickname) {
        this.nick = nick;
    }


    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return pass;
    }

    public String getNickname() {
        return nick;
    }


}
