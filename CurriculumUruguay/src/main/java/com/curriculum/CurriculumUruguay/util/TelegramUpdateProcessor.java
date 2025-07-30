package com.curriculum.CurriculumUruguay.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Utility class to process Telegram updates and determine their type
 */
public class TelegramUpdateProcessor {
    private static final Logger logger = LoggerFactory.getLogger(TelegramUpdateProcessor.class);
    
    /**
     * Determine the type of a Telegram update
     * @param update The Telegram update object
     * @return The type of update (text, photo, document, etc.)
     */
    public static String determineUpdateType(Update update) {
        if (!update.hasMessage()) {
            return "unknown";
        }
        
        Message message = update.getMessage();
        
        if (message.hasText()) {
            return "text";
        } else if (message.hasPhoto()) {
            return "photo";
        } else if (message.hasDocument()) {
            return "document";
        } else if (message.hasVoice()) {
            return "voice";
        } else if (message.hasAudio()) {
            return "audio";
        } else if (message.hasVideo()) {
            return "video";
        } else if (message.hasVideoNote()) {
            return "video_note";
        } else if (message.hasSticker()) {
            return "sticker";
        } else if (message.hasLocation()) {
            return "location";
        } else if (message.hasContact()) {
            return "contact";
        } else {
            return "unknown";
        }
    }
    
    /**
     * Extract caption from a message if available
     * @param message The Telegram message
     * @return The caption or null if not available
     */
    public static String extractCaption(Message message) {
        return message.getCaption();
    }
    
    /**
     * Extract text from a message if available
     * @param message The Telegram message
     * @return The text or null if not available
     */
    public static String extractText(Message message) {
        return message.getText();
    }
}
