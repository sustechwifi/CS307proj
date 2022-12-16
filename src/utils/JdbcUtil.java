package utils;

import utils.annotations.SqlSupport;

import java.sql.*;

public class JdbcUtil {
    private JdbcUtil() {
    }
    public static Connection connection ;

    public static  <T>  Connection getConnection(Class<T> clazz){
        try {
            if (clazz.isAnnotationPresent(SqlSupport.class)) {
                SqlSupport infoAnno =  clazz.getAnnotation(SqlSupport.class);
                Class.forName(infoAnno.DRIVER());
                connection =  DriverManager.getConnection(infoAnno.URL(),infoAnno.USERNAME(),infoAnno.PASSWORD());
                connection.setAutoCommit(false);
                var configs = infoAnno.otherConfigs();
                PreparedStatement ps;
                for (String config : configs) {
                    ps = connection.prepareStatement(config);
                    ps.executeUpdate();
                }
            } else {
                System.out.println("annotation SqlSupport need");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}



