package com.bit.reportservice.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is responsible for interacting with the Gemini API.
 * It provides methods to generate insights based on user input.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String GEMINI_API_KEY;

    /**
     * This method sends a request to the Gemini API to generate an insight based on the provided data.
     *
     * @param data The data to be analyzed.
     * @return The generated insight.
     * @throws IOException If an error occurs during the API request.
     */
    protected String getInsight(String data) throws IOException {
        log.trace("Entering getInsight method in GeminiService");
        String ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + GEMINI_API_KEY;

        JsonObject requestJson = getJsonObject(data);

        // Creating a connection
        HttpURLConnection con = (HttpURLConnection) new URL(ENDPOINT_URL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(requestJson.toString().getBytes());
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            String json = response.toString();
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            JsonArray candidatesArray = jsonObject.getAsJsonArray("candidates");
            for (JsonElement candidateElement : candidatesArray) {
                JsonObject candidateObject = candidateElement.getAsJsonObject();
                JsonObject contentObject = candidateObject.getAsJsonObject("content");
                JsonArray newPartsArray = contentObject.getAsJsonArray("parts");
                for (JsonElement partElement : newPartsArray) {
                    JsonObject partObject = partElement.getAsJsonObject();

                    log.trace("Exiting getInsight method in GeminiService");
                    return partObject.get("text").getAsString();
                }
            }
        }
        log.debug("Gemini connection failed, returning null");

        log.trace("Exiting getInsight method in GeminiService");
        return null;
    }

    /**
     * This method constructs a JSON object that will be sent to the Gemini API.
     * The JSON object includes the user's data, generation configuration, and safety settings.
     *
     * @param data The user's data to be analyzed.
     * @return A JSON object representing the request to the Gemini API.
     */
    private JsonObject getJsonObject(String data) {
        log.trace("Entering getJsonObject method in GeminiService");
        JsonObject requestJson = new JsonObject();
        JsonArray contentsArray = getJsonElements(data);
        requestJson.add("contents", contentsArray);

        // Setting the generation configuration
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 1);
        generationConfig.addProperty("topK", 0);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 8192);

        requestJson.add("generationConfig", generationConfig);

        // Setting the safety settings
        JsonObject safetySettings = new JsonObject();
        safetySettings.addProperty("category", "HARM_CATEGORY_HARASSMENT");
        safetySettings.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        JsonArray safetySettingsArray = new JsonArray();
        safetySettingsArray.add(safetySettings);

        requestJson.add("safetySettings", safetySettingsArray);

        log.trace("Exiting getJsonObject method in GeminiService");
        return requestJson;
    }

    /**
     * This method constructs a JSON array that will be sent to the Gemini API.
     * The JSON array includes a single user content object, which in turn includes a single part object.
     * The part object contains a text field that includes a detailed user request.
     *
     * @param data The user's data to be analyzed.
     * @return A JSON array representing the user's request to the Gemini API.
     */
    private JsonArray getJsonElements(String data) {
        log.trace("Entering getJsonElements method in GeminiService");

        // Setting the prompt string
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text",
                "Suppose that I own a supermarket. Please analyze the sales figures for my products and categorize them into three parts: Low Performance, Moderate Performance, and Best Performance. Provide suggestions such as products whose stock quantities can be reduced and specify the best sellers. Be creative. Use basic words. Exclude additional considerations or recommendations. Give me a straightforward analysis. All of them will be as paragraphs, not item by item. Make it about 200 words." + data);

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray partsArray = new JsonArray();
        partsArray.add(userPart);
        userContent.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);

        log.trace("Exiting getJsonElements method in GeminiService");
        return contentsArray;
    }
}

