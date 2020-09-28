/**
 *
 * @author kan
 */
package com.jkan1.socketchat.server.DataAccessLayer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseOps {

    private static volatile Connection connection;
    private static String userTable = "";
    private static String chatTable = "";

    public static Connection getConnection() throws Exception {

        if (connection == null) {
            final String MYSQL_HOST = System.getenv("MYSQL_HOST");
            final String MYSQL_PORT = System.getenv("MYSQL_PORT");
            final String MYSQL_USER = System.getenv("MYSQL_USER");
            final String MYSQL_PASS = System.getenv("MYSQL_PASS");
            connection = DriverManager.getConnection("jdbc:mysql://" + MYSQL_HOST + ":" + MYSQL_PORT + "/SocketChat?useSSL=false", MYSQL_USER, MYSQL_PASS);
        }
        return connection;
    }

    public static void disconnect() {
        connection = null;
    }

    public static void bootstrap(String userTableName, String chatTableName) {

        userTable = userTableName;
        chatTable = chatTableName;
        int errorCount = 0;
        try {
            createUsersTable(userTable);
        } catch (Exception ex) {
            System.out.println("Bootstrap: " + ex.getMessage());
            errorCount++;
        }
        try {
            createChatTable(chatTable);
        } catch (Exception ex) {
            System.out.println("Bootstrap: " + ex.getMessage());
            errorCount++;
        }
        System.out.println("Bootstrap: " + (errorCount > 0 ? errorCount + " Errors" : "SUCCESS"));
    }

    private static void createUsersTable(String name) throws Exception {
        getConnection();
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE " + name + "(id int primary key auto_increment, name VARCHAR(30), joining_time date)";
        statement.execute(sql);
        disconnect();
    }

    private static void createChatTable(String name) throws Exception {
        getConnection();
        Statement statement = connection.createStatement();
        String sql = "CREATE TABLE " + name + "(msg_id VARCHAR(40) primary key, name VARCHAR(30), msg VARCHAR(200))";
        statement.execute(sql);
        disconnect();
    }

    public static void insertUser(String user) throws Exception {
        getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + userTable + " VALUES (null, ?, ?)");
        preparedStatement.setString(1, user);
        preparedStatement.setDate(2, new Date(System.currentTimeMillis()));
        int rows_affected = preparedStatement.executeUpdate();

        if (rows_affected > 0) {
            System.out.println("User Insert : SUCCESS");
        } else {
            System.out.println("User Insert : FAIL");
        }
        disconnect();
    }

    public static void storeChat(String user, String msg_id, String msg) throws Exception {

        getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + chatTable + " VALUES (?, ?, ?)");
        preparedStatement.setString(1, msg_id);
        preparedStatement.setString(2, user);
        preparedStatement.setString(3, msg);
        int rows_affected = preparedStatement.executeUpdate();

        if (rows_affected > 0) {
            System.out.println("Store Chat : SUCCESS");
        } else {
            System.out.println("Store Chat : FAIL");
        }
        disconnect();

    }

}
