import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DbConnector {

    private static Connection connection;
    private static String nick;
    private static PreparedStatement pstmtGetNickname, pstmtRegisterUser;
    private static String pstmtGetNicknameQuery = "SELECT nick FROM users_t WHERE login = ? AND " +
            "pass = ? ";
    private static String pstmtCreateUserQuery = "INSERT INTO users_t " +
            "(login,pass,nick) " + "VALUES(?, ?, ?, ?);";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found.");
        }
    }

    static void connect() throws SQLException {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/users_for_chat?serverTimezone=Europe/Moscow&useSSL=false", "root", "123456");
            pstmtGetNickname = connection.prepareStatement(pstmtGetNicknameQuery);
            pstmtRegisterUser = connection.prepareStatement(pstmtCreateUserQuery);
        } catch (SQLException e) {
            e.getErrorCode();
        }
    }

    static String getNickname(String login, String pass) {
        if (nick == null) {
            try {
                pstmtGetNickname.setString(1, login);
                pstmtGetNickname.setString(2, pass);
                ResultSet resultSet = pstmtGetNickname.executeQuery();
                System.out.println(resultSet);
                while (resultSet.next()) {
                    nick = resultSet.getString(3);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return nick;
    }

    static void registerUser(String login, String pass) {
        try {
            pstmtRegisterUser.setString(1, login);
            pstmtRegisterUser.setString(2, pass);
            pstmtRegisterUser.setString(3, getNickname(login, pass));
            pstmtRegisterUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
