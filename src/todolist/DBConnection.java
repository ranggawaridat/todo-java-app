package todolist;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection connect() {
        Connection conn = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/todolist",
                "root",
                ""
            );

        } catch (Exception e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
        }

        return conn;
    }
}