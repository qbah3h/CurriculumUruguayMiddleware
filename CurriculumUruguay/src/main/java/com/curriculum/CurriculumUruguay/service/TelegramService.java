package com.curriculum.CurriculumUruguay.service;

import com.curriculum.CurriculumUruguay.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

@Service
public class TelegramService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);
    
    private final Bot telegramBot;
    
    public TelegramService(Bot telegramBot) {
        this.telegramBot = telegramBot;
    }
    
    /**
     * Send a text message to a Telegram user
     * @param userId User ID to send message to
     * @param message Text message to send
     */
    public void sendMessage(Long userId, String message) {
        logger.info("Sending message to user {}: {}", userId, message);
        telegramBot.sendText(userId, message);
    }
    
    /**
     * Send a PDF document to a Telegram user
     * @param userId User ID to send document to
     * @param pdfData PDF file data as byte array
     * @param filename Filename for the PDF
     */
    public void sendPdf(Long userId, byte[] pdfData, String filename) {
        logger.info("Sending PDF to user {}: {}", userId, filename);
        
        try (InputStream inputStream = new ByteArrayInputStream(pdfData)) {
            InputFile document = new InputFile(inputStream, filename);
            telegramBot.sendPdf(userId, document);
        } catch (IOException e) {
            logger.error("Error sending PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending PDF", e);
        }
    }
    
    /**
     * Download a photo from a Telegram message
     * @param photoSizes List of available photo sizes
     * @return The photo data as byte array
     */
    public byte[] downloadPhoto(List<PhotoSize> photoSizes) {
        logger.info("Downloading photo from Telegram");
        
        // Get the largest photo (best quality)
        PhotoSize photo = photoSizes.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .orElseThrow(() -> new RuntimeException("No photos found"));
        
        String fileId = photo.getFileId();
        
        try {
            // Get file path from Telegram servers
            String filePath = telegramBot.getFile(fileId);
            
            // Get the file URL
            String fileUrl = "https://api.telegram.org/file/bot" + telegramBot.getBotToken() + "/" + filePath;
            
            // Download the file
            URL url = new URL(fileUrl);
            try (InputStream inputStream = url.openStream()) {
                return inputStream.readAllBytes();
            }
        } catch (IOException e) {
            logger.error("Error downloading photo: {}", e.getMessage(), e);
            throw new RuntimeException("Error downloading photo", e);
        }
    }
}
