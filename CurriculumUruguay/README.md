# Curriculum Uruguay - Telegram Bot

A Spring Boot application that serves as a middleware between Telegram and a curriculum generation service. This bot allows users to interact via Telegram to generate curriculum vitae in PDF format.

## Project Structure

The project follows a standard Spring Boot structure:

- `com.curriculum.CurriculumUruguay.Bot`: Main Telegram bot class that handles incoming messages
- `com.curriculum.CurriculumUruguay.service`: Service classes for AI communication, database operations, and Telegram interactions
- `com.curriculum.CurriculumUruguay.model`: Data models
- `com.curriculum.CurriculumUruguay.repository`: Database repositories
- `com.curriculum.CurriculumUruguay.util`: Utility classes
- `com.curriculum.CurriculumUruguay.config`: Configuration classes

## Features

- Handles text messages from users
- Processes images with optional captions
- Communicates with an external AI service for curriculum generation
- Sends generated PDF documents back to users
- Logs all interactions in a database

## Setup Instructions

1. Clone the repository
2. Copy `src/main/resources/application.properties.template` to `src/main/resources/application.properties`
3. Edit `application.properties` with your specific configuration:
   - Telegram bot token and username
   - AI service URL
   - Database connection details
4. Build the project with Maven:
   ```
   mvn clean install
   ```
5. Run the application:
   ```
   mvn spring-boot:run
   ```

## Usage

1. Start a chat with your Telegram bot
2. Send a text message describing the curriculum you want to generate
3. Alternatively, send an image (e.g., of your photo for the CV) with a caption
4. The bot will process your request and respond with a generated PDF curriculum

## Dependencies

- Spring Boot 3.x
- Spring Data JPA
- Telegram Bot API (org.telegram:telegrambots)
- MySQL Connector

## Configuration

The application requires the following configuration in `application.properties`:

```properties
# Telegram Bot Configuration
telegram.bot.token=your_telegram_bot_token
telegram.bot.username=your_telegram_bot_username

# AI Service Configuration
ai.service.url=http://your-ai-service-url

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/curriculum_db
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
```

## Notes

- The application expects the AI service to return responses in a specific format with message and PDF data
- PDF data is expected to be Base64 encoded
