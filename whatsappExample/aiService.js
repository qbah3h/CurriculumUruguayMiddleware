const axios = require('axios');
const FormData = require('form-data');

const sendTextToAi = async (aiObj) => {
    console.log(`---------- sendTextToAi ---------- input ${JSON.stringify(aiObj)}`)
    try {

        const requestData = {
            from: aiObj.from,
            userMessage: aiObj.userMessage
        };
        
        const response = await axios.post(process.env.AGENT_URL + "/text", requestData, {
            headers: {
                'Content-Type': 'application/json'
            }
        });
        console.log(`---------- sendTextToAi ---------- Response ${JSON.stringify(response.data.message)}`)
        return response.data; // Process the response as needed
    } catch (error) {
        console.error('Error sending text to AI:', error);
        throw error;
    }
};

const sendImageToAi = async (aiObj) => {
    try {
        // Create a new FormData instance
        const formData = new FormData();
        
        // Append the 'from' parameter
        formData.append('from', aiObj.from);

        // Append the userMessage parameter
        if (aiObj.userMessage && aiObj.userMessage !== "") {
            formData.append('userMessage', aiObj.userMessage);
        } else {
            formData.append('userMessage', "This is the profile image,");
        }

        formData.append('image', aiObj.imgData, 'image.jpg'); // Append the image

        // Make the POST request to send the image
        const response = await axios.post(process.env.AGENT_URL + "/image", formData, {
            headers: {
                'Content-Type': 'multipart/form-data', // Let axios handle the content type for multipart data
            },
            responseType: 'json'
        });
        console.log(`---------- sendImageToAi ---------- Response ${JSON.stringify(response.data)}`);
        return response.data;
        // console.log(`sendImageToAi --- Response ${JSON.stringify(response.data)}`)
        //return Buffer.from(response.data); // Process the response as needed
    } catch (error) {
        console.error('Error sending image to AI:', error);
        throw error;
    }
};

module.exports = {
    sendImageToAi,
    sendTextToAi
};