package org.quickbitehub;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // Using try-with-resources to allow autoclose to run upon finishing
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            QuickBite quickBiteBot = new QuickBite();
            botsApplication.registerBot(quickBiteBot.getBotToken(), quickBiteBot);

            System.out.println("The Bot is successfully started!");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url="jdbc:postgresql://localhost:5432/[database name]";
        String username="postgres";
        String password="";

        try {
            Connection con = DriverManager.getConnection(url, username, password);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM Customer WHERE id=2");
            rs.next();
            String name = rs.getString(1);
            System.out.println(name);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



    }
}