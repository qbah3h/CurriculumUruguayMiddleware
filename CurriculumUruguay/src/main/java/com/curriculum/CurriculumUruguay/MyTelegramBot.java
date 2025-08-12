package com.curriculum.CurriculumUruguay;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

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
    sendImageWithButton(chatId, "templates/plain.jpeg", "Image 1", "image1");
    sendImageWithButton(chatId, "templates/modern.jpeg", "Image 2", "image2");
    sendImageWithButton(chatId, "templates/plain.jpeg", "Image 3", "image3");
}

private void sendImageWithButton(Long chatId, String imagePath, String buttonText, String callbackData) throws TelegramApiException {
    InputStream imageStream = getClass().getClassLoader().getResourceAsStream(imagePath);
    if (imageStream == null) {
        System.err.println("Image not found: " + imagePath);
        return;
    }

    // Create button for this image
    InlineKeyboardButton button = new InlineKeyboardButton(buttonText);
    button.setCallbackData(callbackData);
    List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
    keyboard.add(List.of(button));
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboard);

    // Send photo with inline button
    SendPhoto photo = new SendPhoto();
    photo.setChatId(chatId.toString());
    photo.setPhoto(new InputFile(imageStream, imagePath));
    photo.setReplyMarkup(markup);

    execute(photo);
}


    private void sendSelectedImage(Long chatId, String imageKey) throws TelegramApiException {
        String imagePath;
        switch (imageKey) {
            case "image1": imagePath = "templates/plain.jpeg"; break;
            case "image2": imagePath = "templates/modern.jpeg"; break;
            case "image3": imagePath = "templates/modern.jpeg"; break;
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
        photo.setPhoto(new InputFile(imageStream, imagePath));

        execute(photo);
    }
}
