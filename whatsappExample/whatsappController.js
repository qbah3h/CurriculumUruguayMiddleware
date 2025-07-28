const { createWppInteraction } = require('../services/databaseService');
const { sendImageToAi, sendTextToAi } = require('../services/aiService');
const { sendWppMessage, sendWppPdf, downloadImage, markMessageAsRead } = require('../services/whatsappService');
const { processWhatsAppWebhook } = require('../utils/whatsappProcessor');

const handleWppInMessage = async (req, res) => {
    try {
        // Process the incoming webhook data
        const wppObj = req.body;
        const events = processWhatsAppWebhook(wppObj);

        // Process each event from the webhook
        for (const event of events) {
            console.log("---------- handleWppInMessage ----------");
            
            // Extract common event data
            const fromNumber = event.from;
            const toNumberId = event.phone_number_id;
            const eventType = event.type;
            
            // Save the interaction to database
            await createWppInteraction({
                fromNumber,
                toNumberId,
                eventType,
                eventData: event
            });

            // Mark the message as read if applicable
            if (event.message_id && event.phone_number_id) {
                markMessageAsRead(event.phone_number_id, event.message_id);
            }

            // Prepare AI request object
            const aiObj = {
                userMessage: "",
                from: fromNumber,
                imgData: null
            };

            // Process different event types
            let responseFromAi;
            if (eventType === 'text') {
                aiObj.userMessage = event.body;
                responseFromAi = await sendTextToAi(aiObj);
            } else if (eventType === 'image') {
                // Download and process image
                const image = await downloadImage({
                    mime_type: event.mime_type,
                    sha256: event.sha256,
                    id: event.id,
                });
                
                // Add caption if available
                if (event.caption) {
                    aiObj.userMessage = event.caption;
                }
                
                aiObj.imgData = image;
                responseFromAi = await sendImageToAi(aiObj);
            }

            // Send responses back to the user
            if (responseFromAi) {
                // Send PDF if available
                if (responseFromAi.message && responseFromAi.message.pdfData) {
                    await sendWppPdf(
                        fromNumber, 
                        toNumberId, 
                        responseFromAi.message.pdfData, 
                        responseFromAi.message.pdfFilename
                    );
                }

                // Send text message if available
                if (responseFromAi.message && responseFromAi.message.message) {
                    await sendWppMessage(fromNumber, toNumberId, responseFromAi.message.message);
                }
            }
        }

        // Respond to the incoming request with a success status
        res.sendStatus(200);
    } catch (error) {
        console.error('Error handling WhatsApp message:', error.message);
        res.status(500).send('Internal Server Error');
    }
};

const handleWppValidation = async (req, res) => {
    const info = req.query;
    console.log(`From the get: ${JSON.stringify(info)}`);
    if (
        req.query['hub.mode'] == 'subscribe' &&
        req.query['hub.verify_token'] == process.env.APP_SECRET
    ) {
        res.send(req.query['hub.challenge']);
    } else {
        res.sendStatus(400);
    }
}

module.exports = {
    handleWppInMessage,
    handleWppValidation
};