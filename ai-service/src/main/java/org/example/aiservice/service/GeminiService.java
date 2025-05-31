package org.example.aiservice.service;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-05-20:generateContent";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(300, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    // Lưu lịch sử hội thoại
    private final List<String> conversationHistory = new ArrayList<>();

    public synchronized String chat(String text) throws IOException {
        // Thêm câu hỏi mới vào lịch sử
        conversationHistory.add(text);

        // Tạo danh sách parts từ lịch sử hội thoại
        List<Map<String, Object>> parts = new ArrayList<>();
        for (String msg : conversationHistory) {
            parts.add(Map.of("text", msg));
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", parts)
                )
        );

        String json = gson.toJson(body);

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + apiKey)
                .addHeader("Connection", "keep-alive")
                .addHeader("Content-Type", "text/plain")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response);
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            String reply = jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            // Thêm câu trả lời vào lịch sử
            conversationHistory.add(reply);

            return reply;
        }
    }
}
