package main.utils;

import main.utils.annotations.SqlSupport;

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

    public static void connect(String url,String username,String password){
        try {
            connection = DriverManager.getConnection(url,username,password);
            connection.setAutoCommit(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String[] otherConfigs = {
            "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO sustcmanager;",
            "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO seaportofficer;",
            "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO courier;",
            "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO companymanager;",
            "grant select on undertake, city, company, container,staff,ship,record to sustcmanager,courier,seaportofficer,companymanager;",
            "grant insert on undertake, record to courier;",
            "grant update on record ,   undertake to seaportofficer, courier;",
            "grant update on record,    container,ship to companymanager;"
    };

    public static void loadGrand(){
        if (connection == null){
            return;
        }
        var configs = otherConfigs;
        PreparedStatement ps;
        try {
            for (String config : configs) {
                ps = connection.prepareStatement(config);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}



