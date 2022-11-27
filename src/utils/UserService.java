package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserService {

    public static boolean login(User user) {
        Connection connection = JdbcUtil.connection;
        String sql = "select username, password from game.public.user where username = ? and password = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                System.out.println("username or password is incorrect");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCount(String username, boolean isWin) {
        Connection connection = JdbcUtil.connection;
        String sql1 = "update game.public.user set count = count + 1 where username = ?";
        String sql2 = "update game.public.user set count = count + 1,win_count = win_count + 1 where username = ?";
        try {
            if (checkName(username)) {
                System.out.println("wrong username");
                return false;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(isWin ? sql2 : sql1);
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean checkName(String username) {
        Connection connection = JdbcUtil.connection;
        String sql = "select id from game.public.user where username = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean register(User user) {
        Connection connection = JdbcUtil.connection;
        String sql = "insert into game.public.user (username, password) values(?,?)";
        try {
            if (!checkName(user.getUsername())) {
                System.out.println("the username is used");
                return false;
            }
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.executeUpdate();
            System.out.println("register successfully");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User getUser(String username) {
        Connection connection = JdbcUtil.connection;
        String sql = "select * from game.public.user where username = ?";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);
            ResultSet r = preparedStatement.executeQuery();
            r.next();
            return new User(
                    r.getInt(1), r.getString(2),
                    r.getString(3), r.getInt(4),
                    r.getInt(5)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

