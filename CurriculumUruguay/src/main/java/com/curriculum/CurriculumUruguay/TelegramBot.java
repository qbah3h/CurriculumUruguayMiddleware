package com.curriculum.CurriculumUruguay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.InputStream;
import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    // Store selected image for each user
    private final Map<String, String> userSelectedImage = new HashMap<>();

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;


    @Override
    public String getBotUsername() {
        return botUsername; // change to your bot's username
    }

    @Override
    public String getBotToken() {
        return botToken; // change to your bot's token
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                String chatId = update.getMessage().getChatId().toString();

                sendImageSelectionMenu(chatId);
            } else if (update.hasCallbackQuery()) {
                String data = update.getCallbackQuery().getData();
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                if (data.startsWith("select_")) {
                    String imageKey = data.replace("select_", "");
                    userSelectedImage.put(chatId, imageKey);

                    // Preview selected image
                    sendPhoto(chatId, imageKey, "Preview of your selected image.");
                    sendSendButton(chatId);

                } else if (data.equals("send_selected")) {
                    String imageUrl = userSelectedImage.get(chatId);
                    if (imageUrl != null) {
                        sendPhoto(chatId, imageUrl, "Here is your selected image!");
                    } else {
                        sendMessage(chatId, "Please select an image first.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImageSelectionMenu(String chatId) throws Exception {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                InlineKeyboardButton.builder().text("Img 1").callbackData("select_image1").build(),
                InlineKeyboardButton.builder().text("Img 2").callbackData("select_image2").build(),
                InlineKeyboardButton.builder().text("Img 3").callbackData("select_image3").build()
        ));

        markup.setKeyboard(rows);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose an image:")
                .replyMarkup(markup)
                .build();

        execute(message);
    }

    private void sendSendButton(String chatId) throws Exception {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder().text("Send").callbackData("send_selected").build()
        ));
        markup.setKeyboard(rows);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Click send to confirm.")
                .replyMarkup(markup)
                .build();

        execute(message);
    }

    // Map logical keys to classpath resource paths
    private String resolveImagePath(String key) {
        switch (key) {
            case "image1": return "templates/plain.jpeg";
            case "image2": return "templates/modern.jpeg";
            case "image3": return "templates/modern.jpeg";
            default: return null;
        }
    }

    private void sendPhoto(String chatId, String imageUrl, String caption) throws Exception {
        String path = resolveImagePath(imageUrl);
        if (path == null) {
            sendMessage(chatId, "Unknown image selection. Please try again.");
            return;
        }

        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(path);
        if (imageStream == null) {
            sendMessage(chatId, "Image not found on server: " + path);
            return;
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new org.telegram.telegrambots.meta.api.objects.InputFile(imageStream, path));
        photo.setCaption(caption);
        execute(photo);
    }

    private void sendMessage(String chatId, String text) throws Exception {
        execute(SendMessage.builder().chatId(chatId).text(text).build());
    }
}