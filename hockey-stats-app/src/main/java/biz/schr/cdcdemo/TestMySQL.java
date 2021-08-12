package biz.schr.cdcdemo;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestMySQL {
    public static void main(String[] args) {
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(Config.dbURL, Config.dbUsername, Config.dbPassword);
            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Database Connection Terminated");
                } catch (Exception e) {}
            }
        }
    }
}
