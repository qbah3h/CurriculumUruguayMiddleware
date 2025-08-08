package com.curriculum.CurriculumUruguay;

import com.curriculum.CurriculumUruguay.service.AiService;
import com.curriculum.CurriculumUruguay.util.TelegramUpdateProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;
    
    private final AiService aiService;
    
    public Bot(AiService aiService) {
        this.aiService = aiService;
    }

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
        logger.info("Received update: {}", update.getUpdateId());
        
        if (!update.hasMessage()) {
            logger.info("Update doesn't contain a message, ignoring");
            return;
        }
        
        Message msg = update.getMessage();
        Long userId = msg.getFrom().getId();
        
        // Determine the type of update
        String eventType = TelegramUpdateProcessor.determineUpdateType(update);
        logger.info("Update type: {}", eventType);
        
        // Log the interaction instead of saving to database
        logger.info("User interaction: userId={}, eventType={}", userId, eventType);
        
        try {
            // Process different types of messages
            switch (eventType) {
                case "text":
                    handleTextMessage(update);
                    break;
                case "photo":
                    handlePhotoMessage(update);
                    break;
                case "document":
                    handleDocumentMessage(update);
                    break;
                default:
                    // For unsupported message types, send a default response
                    sendText(userId, "Lo siento, no puedo procesar este tipo de mensaje. Por favor envía texto o una imagen.");
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing update: {}", e.getMessage(), e);
            sendText(userId, "Lo siento, ocurrió un error al procesar tu mensaje. Por favor intenta nuevamente.");
        }
    }
    
    /**
     * Handle text messages
     */
    private void handleTextMessage(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        String text = message.getText();

        if(text.equals("/start")) {
            return;
        }
        
        logger.info("Processing text message from user {}: {}", userId, text);
        
        // Send text to AI service
        Map<String, Object> aiResponse = aiService.sendTextToAi(userId.toString(), text);
        
        // Process AI response
        processAiResponse(userId, aiResponse);
    }
    
    /**
     * Handle photo messages
     */
    private void handlePhotoMessage(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        List<PhotoSize> photos = message.getPhoto();
        String caption = message.getCaption();
        
        logger.info("Processing photo message from user {}", userId);
        
        try {
            // Get the largest photo (best quality)
            PhotoSize photo = photos.stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .orElseThrow(() -> new RuntimeException("No photos found"));
            
            // Download the photo
            byte[] photoData = downloadPhotoData(photo.getFileId());
            
            // Send photo to AI service
            Map<String, Object> aiResponse = aiService.sendImageToAi(userId.toString(), caption, photoData);
            
            // Process AI response
            processAiResponse(userId, aiResponse);
        } catch (Exception e) {
            logger.error("Error processing photo: {}", e.getMessage(), e);
            sendText(userId, "Lo siento, ocurrió un error al procesar tu imagen. Por favor intenta nuevamente.");
        }
    }
    
    /**
     * Handle document messages
     */
    private void handleDocumentMessage(Update update) {
        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        
        // For now, just inform the user that we don't process documents
        sendText(userId, "Lo siento, actualmente no procesamos documentos. Por favor envía texto o una imagen.");
    }
    
    /**
     * Process the response from the AI service
     */
    private void processAiResponse(Long userId, Map<String, Object> aiResponse) {
        if (aiResponse == null) {
            sendText(userId, "Lo siento, no pude obtener una respuesta del servicio. Por favor intenta nuevamente.");
            return;
        }
        // Extract nested "message" map
        Map<String, Object> messageMap = (Map<String, Object>) aiResponse.get("message");


        // Send PDF if available
        if (messageMap.containsKey("pdfData") && messageMap.get("pdfData") != null) {
            try {
                String pdfData = (String) messageMap.get("pdfData");
                logger.info("PDF data received: [data available]");

                if (pdfData.isEmpty()) {
                    logger.warn("PDF data is empty");
                    return;
                }

                String filename = messageMap.containsKey("pdfFilename") && messageMap.get("pdfFilename") != null ?
                        (String) messageMap.get("pdfFilename") : "curriculum.pdf";

                try {
                    byte[] pdfBytes = Base64.getDecoder().decode(pdfData);

                    String textMessage = (String) messageMap.get("message");

                    // Send the PDF
                    sendPdfWithCaption(userId, pdfBytes, filename, textMessage);

                    return;
                } catch (IllegalArgumentException e) {
                    // This exception is thrown when the input is not valid Base64
                    logger.error("Invalid Base64 data for PDF: {}", e.getMessage());
                    sendText(userId, "Lo siento, los datos del PDF no son válidos. Por favor intenta nuevamente.");
                }
            } catch (Exception e) {
                logger.error("Error processing PDF data: {}", e.getMessage(), e);
                sendText(userId, "Lo siento, ocurrió un error al procesar el PDF. Por favor intenta nuevamente.");
            }
        }
        // Send text message if available
        if (messageMap.containsKey("message") && messageMap.get("message") instanceof String) {
            String textMessage = (String) messageMap.get("message");
            sendText(userId, textMessage);
        }

         else if (!aiResponse.containsKey("message")) {
            // If the response format is unexpected, send a generic message
            sendText(userId, "He recibido tu mensaje, pero no pude procesar la respuesta correctamente.");
        }
    }
    
    /**
     * Download photo data from Telegram servers
     */
    private byte[] downloadPhotoData(String fileId) throws IOException, TelegramApiException {
        logger.info("Downloading photo with fileId: {}", fileId);
        
        // Get file path from Telegram servers
        String filePath = getFile(fileId);
        logger.info("Retrieved file path from Telegram: {}", filePath);
        
        // Get the file URL
        String fileUrl = "https://api.telegram.org/file/bot" + botToken + "/" + filePath;
        logger.info("File URL: {}", fileUrl);
        
        // Download the file
        URL url = new URL(fileUrl);
        try (InputStream inputStream = url.openStream()) {
            byte[] imageData = inputStream.readAllBytes();
            logger.info("Successfully downloaded image data, size: {} bytes", imageData.length);
            return imageData;
        } catch (IOException e) {
            logger.error("Error downloading image data: {}", e.getMessage());
            throw e;
        }
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            logger.error("Error sending text message: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void sendPdf(Long who, InputFile document) {
        SendDocument sd = SendDocument.builder()
                .chatId(who.toString())
                .document(document)
                .caption("Aquí está tu curriculum")
                .build();
        try {
            execute(sd);
        } catch (TelegramApiException e) {
            logger.error("Error sending PDF: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    
    public void sendPdfWithCaption(Long who, byte[] pdfData, String filename, String caption) {
        try (InputStream inputStream = new ByteArrayInputStream(pdfData)) {
            InputFile document = new InputFile(inputStream, filename);
            
            SendDocument sd = SendDocument.builder()
                    .chatId(who.toString())
                    .document(document)
                    .caption(caption)
                    .build();
            
            execute(sd);
        } catch (IOException | TelegramApiException e) {
            logger.error("Error sending PDF with caption: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getFile(String fileId) {
        GetFile gf = GetFile.builder()
                .fileId(fileId)
                .build();

        try {
            return execute(gf).getFilePath();
        } catch (TelegramApiException e) {
            logger.error("Error getting file: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())
                .chatId(who.toString())
                .messageId(msgId)
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            logger.error("Error copying message: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
