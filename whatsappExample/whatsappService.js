const axios = require('axios');
const FormData = require('form-data');

const apiVersion = process.env.API_VERSION;
const accessToken = process.env.ACCESS_TOKEN;

/**
 * Send a WhatsApp message using the WhatsApp API.
 * @param {string} toNumberId - The recipient's phone number id. 
 * @param {string} fromNumber - The sender's phone number. (The number in Meta)
 * @param {string} message - The message content.
 * @returns {Promise<void>}
 */
const sendWppMessage = async (fromNumber, toNumberId, message) => {
    try {
        console.log(`---------- sendWppMessage ---------- ${JSON.stringify({ fromNumber, toNumberId, message })}`)

        // Check if required fields are present
        if (!toNumberId || !fromNumber || !message) {
            throw new Error('Missing required fields: toNumberId, fromNumber, or message.');
        }

        const messageObject = {
            messaging_product: 'whatsapp',
            recipient_type: 'individual',
            to: fromNumber,  // Correcting the 'to' field
            type: 'text',
            text: {
                preview_url: true,
                body: message,
            },
        };

        // Make the API request
        await axios.post(
            `https://graph.facebook.com/${apiVersion}/${toNumberId}/messages`,
            messageObject,
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'  // Adding Content-Type header
                },
            }
        );
    } catch (error) {
        console.error('Error sending message via WhatsApp API:', error.response ? error.response.data : error.message);
        throw error;
    }
};


// Function to get the media URL from WhatsApp API
const getMediaUrl = async (mediaId) => {
    try {
        const response = await axios.get(`https://graph.facebook.com/${apiVersion}/${mediaId}`, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
        });
        return response.data.url;  // This will be the URL of the media (image)
    } catch (error) {
        console.error('Error fetching media URL:', error);
        throw error;
    }
};

// Function to download the image from the URL
const downloadImage = async (imageData) => {
    try {
        const mediaUrl = await getMediaUrl(imageData.id);

        const response = await axios.get(mediaUrl, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
            },
            responseType: 'stream',
        });

        // Create a FormData instance and append the image stream directly
        const formData = new FormData();
        // console.log(`downloadImage --- Response ${JSON.stringify(response.data)}`)
        formData.append('image', response.data, 'image.jpg');
        return formData;
    } catch (error) {
        console.error('Error downloading the image:', error);
        throw error;
    }
};

const sendWppPdf = async (fromNumber, toNumberId, pdf, filename) => {
    try {
        // Check if required fields are present
        console.log(`---------- sendWppPdf ---------- ${JSON.stringify({ fromNumber, toNumberId, pdf, filename })}`)

        if (!toNumberId || !fromNumber || !pdf) {
            throw new Error('Missing required fields: toNumberId, fromNumber, or pdf.');
        }

        const response = await uploadMedia(toNumberId, pdf);

        const messageObject = {
            messaging_product: 'whatsapp',
            recipient_type: 'individual',
            to: fromNumber,  // Correcting the 'to' field
            type: 'document',
            document: {
                id: response.id,
                // link: message,
                // caption: message, // optional
                filename: filename // optional
            },
        };

        // Make the API request
        await axios.post(
            `https://graph.facebook.com/${apiVersion}/${toNumberId}/messages`,
            messageObject,
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'  // Adding Content-Type header
                },
            }
        );
    } catch (error) {
        console.error('Error sending pdf via WhatsApp API:', error.response ? error.response.data : error.message);
        throw error;
    }
};

const uploadMedia = async (phoneNumberId, media) => {
    try {
        const mediaBuffer = Buffer.from(media);

        const formData = new FormData();
        formData.append('messaging_product', 'whatsapp');
        formData.append('file', mediaBuffer, {
            filename: "cv.pdf",
            contentType: "application/pdf",
        });

        const response = await axios.post(`https://graph.facebook.com/${apiVersion}/${phoneNumberId}/media`,
            formData, {
            headers: {
                Authorization: `Bearer ${accessToken}`,
                ...formData.getHeaders()
            },
            maxBodyLength: Infinity
        });
        console.log('uploadMedia --- Response:', {
            status: response.status,
            data: response.data,
            id: response.data.id,
            headers: response.headers
        });

        return response.data;  // This will be the URL of the media (image)
    } catch (error) {
        console.error('Error fetching media URL:', error);
        throw error;
    }
};


/**
 * Mark a WhatsApp message as read using the WhatsApp API.
 * @param {string} phoneNumberId - The WhatsApp Business Phone Number ID.
 * @param {string} messageId - The ID of the message to mark as read.
 * @returns {Promise<void>}
 */
const markMessageAsRead = async (phoneNumberId, messageId) => {
    try {
        console.log(`---------- markMessageAsRead ---------- ${JSON.stringify({ phoneNumberId, messageId })}`);

        // Check if required fields are present
        if (!phoneNumberId || !messageId) {
            throw new Error('Missing required fields: phoneNumberId or messageId.');
        }

        const requestBody = {
            messaging_product: 'whatsapp',
            status: 'read',
            message_id: messageId,
            typing_indicator: {
                type: "text"
            }
        };

        // Make the API request
        const response = await axios.post(
            `https://graph.facebook.com/${apiVersion}/${phoneNumberId}/messages`,
            requestBody,
            {
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    'Content-Type': 'application/json'
                },
            }
        );

        console.log(`Message ${messageId} marked as read successfully`, response.data);
    } catch (error) {
        console.error('Error marking message as read via WhatsApp API:', error.response ? error.response.data : error.message);
        // Don't throw the error to prevent disrupting the main flow
    }
};

module.exports = {
    sendWppMessage,
    sendWppPdf,
    downloadImage,
    markMessageAsRead
};