package org.quickbitehub;

import io.github.cdimascio.dotenv.Dotenv;
import org.quickbitehub.client.Account;
import org.quickbitehub.client.NavigationState;
import org.quickbitehub.client.User;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashMap;
import java.util.Stack;

public class QuickBite implements LongPollingSingleThreadUpdateConsumer {
    private final String botToken;
    private final String botUsername = "QuickBiteHub_bot";
    private TelegramClient telegramClient;
    private HashMap<Long, Account> usersAccounts;
    private HashMap<Long, Stack<NavigationState>> usersState = new HashMap<>();

    public QuickBite() {
        Dotenv dotenv = Dotenv.load();
        this.botToken = dotenv.get("BOT_TOKEN");
        this.telegramClient = new OkHttpTelegramClient(this.botToken);
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getBotToken() {
        return botToken;
    }

    public boolean isAccountExist(String telegramId) {
        return usersAccounts.containsKey(telegramId);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().contains("/")) {
            botCommandsHandler(update.getMessage());
        } else if (update.hasCallbackQuery()) {

        }

        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            try {
                sendTextMessage(chat_id, message_text);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void botCommandsHandler(Message message) {

    }

    private void sendTextMessage(long user, String textMessage) throws TelegramApiException{
        SendMessage msg = SendMessage
                .builder()
                .chatId(user)
                .text(textMessage)
                .build();

        telegramClient.execute(msg);
    }
}