package com.curriculum.CurriculumUruguay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {
    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Value("${ai.service.url}")
    private String agentUrl;

    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Send text message to AI service
     * @param from User identifier
     * @param userMessage Text message from user
     * @return Response from AI service
     */
    public Map<String, Object> sendTextToAi(String from, String userMessage) {
        logger.info("---------- sendTextToAi ---------- input from: {}, message: {}", from, userMessage);
        
        Map<String, String> requestData = new HashMap<>();
        requestData.put("from", from);
        requestData.put("userMessage", userMessage);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestData, headers);
        
        try {
            // First, make the request and get the raw response entity without type conversion
            ResponseEntity<byte[]> response = restTemplate.exchange(
                agentUrl + "/text",
                HttpMethod.POST,
                requestEntity,
                byte[].class
            );

            // Create a response map to return
            Map<String, Object> responseMap = new HashMap<>();

            // Handle JSON response - parse the byte array back to a Map
            try {
                String jsonString = new String(response.getBody());
                ObjectMapper mapper = new ObjectMapper();
                responseMap = mapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
            } catch (IOException e) {
                logger.error("Error parsing JSON response: {}", e.getMessage(), e);
                responseMap.put("message", "Error parsing JSON response");
            }

            logger.info("---------- sendTextToAi ---------- responseMap: {}", responseMap);

            // Extract nested "message" map
            Map<String, Object> messageMap = (Map<String, Object>) responseMap.get("message");

            String messageText = (String) messageMap.get("message");
            String status = (String) messageMap.get("status");
            String pdfFilename = (String) messageMap.get("pdfFilename");

            logger.info("---------- sendTextToAi ---------- status: {}", status);
            logger.info("---------- sendTextToAi ---------- pdfFilename: {}", pdfFilename);
            logger.info("---------- sendTextToAi ---------- messageText: {}", messageText);
            return responseMap;
        } catch (Exception e) {
            logger.error("Error sending text to AI: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending text to AI", e);
        }
    }

    /**
     * Send image to AI service
     * @param from User identifier
     * @param userMessage Optional caption or message
     * @param imageData Image data
     * @return Response from AI service
     */
    public Map<String, Object> sendImageToAi(String from, String userMessage, byte[] imageData) {
        logger.info("---------- sendImageToAi ---------- input from: {}, message: {}", from, userMessage);
        
        // Log image data presence and size
        if (imageData == null) {
            logger.error("Image data is null");
        } else {
            logger.info("Image data is present, size: {} bytes", imageData.length);
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("from", from);
        
        // Add userMessage if available
        if (userMessage != null && !userMessage.isEmpty()) {
            body.add("userMessage", userMessage);
        } else {
            body.add("userMessage", "This is the profile image");
        }
        
        // Add image data using ByteArrayResource instead of HttpEntity
        if (imageData != null && imageData.length > 0) {
            ByteArrayResource imageResource = new ByteArrayResource(imageData) {
                @Override
                public String getFilename() {
                    return "image.jpg"; // Provide a filename for the multipart request
                }
            };
            body.add("image", imageResource);
            logger.info("Added image to request as ByteArrayResource");
        } else {
            logger.error("Cannot add image to request: image data is empty or null");
        }
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        // Log request details
        logger.info("Request headers: {}", headers);
        logger.info("Request body contains keys: {}", body.keySet());
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                agentUrl + "/image",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            logger.info("---------- sendImageToAi ---------- Response: {}", response.getBody());
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error sending image to AI: {}", e.getMessage(), e);
            throw new RuntimeException("Error sending image to AI", e);
        }
    }
}
