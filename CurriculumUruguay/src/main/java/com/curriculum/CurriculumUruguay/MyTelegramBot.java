package com.curriculum.CurriculumUruguay;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                // Step 1: Show inline keyboard with image choices
                sendImageChoiceMenu(update.getMessage().getChatId());
            }
            else if (update.hasCallbackQuery()) {
                // Step 2: Send the selected image
                String callbackData = update.getCallbackQuery().getData();
                Long chatId = update.getCallbackQuery().getMessage().getChatId();
                sendSelectedImage(chatId, callbackData);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendImageChoiceMenu(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Choose an image:");

        // Create buttons
        InlineKeyboardButton img1 = new InlineKeyboardButton("Image 1");
        img1.setCallbackData("plain");

        InlineKeyboardButton img2 = new InlineKeyboardButton("Image 2");
        img2.setCallbackData("modern");

        InlineKeyboardButton img3 = new InlineKeyboardButton("Image 3");
        img3.setCallbackData("modern");

        // Arrange buttons in a row
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(img1);
        row.add(img2);
        row.add(img3);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        execute(message);
    }

    private void sendSelectedImage(Long chatId, String imageKey) throws TelegramApiException {
        String imagePath;
        switch (imageKey) {
            case "image1": imagePath = "templates/plain.jpg"; break;
            case "image2": imagePath = "templates/modern.jpg"; break;
            case "image3": imagePath = "templates/modern.jpg"; break;
            default: return;
        }

        // Load image from resources
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
        if (imageStream == null) {
            System.err.println("Image not found: " + imagePath);
            return;
        }

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId.toString());
        photo.setPhoto(imageStream);

        execute(photo);
    }

    @Override
    public String getBotUsername() {
        return "YOUR_BOT_USERNAME";
    }

    @Override
    public String getBotToken() {
        return "YOUR_BOT_TOKEN";
    }
}
