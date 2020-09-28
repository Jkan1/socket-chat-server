/**
 *
 * @author kan
 */
package com.jkan1.socketchat.server;

import com.jkan1.socketchat.server.DataAccessLayer.DatabaseOps;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class Server {

    private static ArrayList<String> users = new ArrayList<>();
    private static ArrayList<MessagingThread> clients = new ArrayList<>();
    private static String EXIT_CODE = "<EXIT_SOCKET_CHAT_SERVER>";

    public static void main(String[] args) throws Exception {
        final int SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));
        ServerSocket server = new ServerSocket(SERVER_PORT, 10);
        out.println("SocketChat-Server is running on port " + SERVER_PORT);
        DatabaseOps.bootstrap("Users", "Chat");
        while (true) {
            Socket client = server.accept();
            MessagingThread thread = new MessagingThread(client);
            clients.add(thread);
            thread.start();
        }
    }

    public static void sendToAll(String user, String message, boolean sendToSelf) {

        for (MessagingThread c : clients) {
            if (!c.getUser().equals(user)) {
                c.sendMessage(user, message, sendToSelf);
            } else {
                if (sendToSelf) {
                    c.sendToMe(user, message);
                }
            }
        }
    }

    static class MessagingThread extends Thread {

        String user = "";
        BufferedReader input;
        PrintWriter output;

        public MessagingThread(Socket client) throws Exception {

            input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            output = new PrintWriter(client.getOutputStream(), true);

            user = input.readLine();
            users.add(user);

            DatabaseOps.insertUser(user);
        }

        public void sendMessage(String chatUser, String msg, boolean userActive) {
            if (userActive) {
                output.println(chatUser + ": " + msg);
            } else {
                output.println(msg);
            }
        }

        public void sendToMe(String chatUser, String msg) {
            output.println("You: " + msg);
        }

        public String getUser() {
            return user;
        }

        public void saveInDB(String chatUser, String msg) throws Exception {
            String msg_id = chatUser + "_" + System.currentTimeMillis();
            DatabaseOps.storeChat(user, msg_id, msg);
        }

        @Override
        public void run() {
            String line;
            try {
                while (true) {
                    line = input.readLine();
                    if (line.equals(EXIT_CODE)) {
                        clients.remove(this);
                        users.remove(user);
                        sendToAll(user, user + " left chat...", false);
                        break;
                    } else {
                        sendToAll(user, line, true);
                        saveInDB(user, line);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex);
            }
        }
    }
}
