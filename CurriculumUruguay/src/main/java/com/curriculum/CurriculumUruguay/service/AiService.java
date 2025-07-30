package com.curriculum.CurriculumUruguay.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            ResponseEntity<Map> response = restTemplate.exchange(
                agentUrl + "/text",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            logger.info("---------- sendTextToAi ---------- Response: {}", response.getBody());
            return response.getBody();
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
        
        // Add image data
        HttpHeaders imageHeaders = new HttpHeaders();
        imageHeaders.setContentType(MediaType.IMAGE_JPEG);
        HttpEntity<byte[]> imageEntity = new HttpEntity<>(imageData, imageHeaders);
        body.add("image", imageEntity);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
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
