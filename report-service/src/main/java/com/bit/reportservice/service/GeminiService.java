package com.bit.reportservice.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api-key}")
    private String GEMINI_API_KEY;

    protected String getInsight(String data) throws IOException {
        String ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + GEMINI_API_KEY;

        JsonObject requestJson = getJsonObject(data);

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
                    return partObject.get("text").getAsString();
                }
            }
        }
        return null;
    }

    private JsonObject getJsonObject(String data) {
        JsonObject requestJson = new JsonObject();
        JsonArray contentsArray = getJsonElements(data);
        requestJson.add("contents", contentsArray);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 1);
        generationConfig.addProperty("topK", 0);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("maxOutputTokens", 8192);

        requestJson.add("generationConfig", generationConfig);

        JsonObject safetySettings = new JsonObject();
        safetySettings.addProperty("category", "HARM_CATEGORY_HARASSMENT");
        safetySettings.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
        JsonArray safetySettingsArray = new JsonArray();
        safetySettingsArray.add(safetySettings);

        requestJson.add("safetySettings", safetySettingsArray);
        return requestJson;
    }

    private JsonArray getJsonElements(String data) {
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text",
                " Suppose that I own a supermarket. Please analyze the sales figures for my products and categorize them into three parts: Low Performance, Moderate Performance, and Best Performance. Provide suggestions such as products whose stock quantities can be reduced and specify the best sellers. Be creative. Use basic words. Exclude additional considerations or recommendations. Give me a straightforward analysis. All of them will be as paragraphs, not item by item. Make it about 200 words." + data);

        JsonObject userContent = new JsonObject();
        userContent.addProperty("role", "user");
        JsonArray partsArray = new JsonArray();
        partsArray.add(userPart);
        userContent.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(userContent);
        return contentsArray;
    }
}

