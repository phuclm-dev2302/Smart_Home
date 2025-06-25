package org.example.aiservice.service;

import com.google.gson.*;
import okhttp3.*;
import org.example.aiservice.dto.PostDocument;
import org.example.aiservice.dto.PostSearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.*;
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
    private final List<String> conversationHistory = new ArrayList<>();

    public void resetConversation() {
        conversationHistory.clear();
    }

    private synchronized void addToHistory(String msg) {
        conversationHistory.add(msg);
    }

    private synchronized List<Map<String, Object>> getHistoryParts() {
        List<Map<String, Object>> parts = new ArrayList<>();
        for (String msg : conversationHistory) {
            parts.add(Map.of("text", msg));
        }
        return parts;
    }

    public Mono<String> chat(String text) {
        return Mono.fromCallable(() -> {
            addToHistory(text);
            Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", getHistoryParts())));
            return sendToGemini(body, true);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> chatSingle(String text) {
        return Mono.fromCallable(() -> {
            Map<String, Object> body = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", text)))));
            return sendToGemini(body, false);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String sendToGemini(Map<String, Object> body, boolean saveHistory) throws IOException {
        String json = gson.toJson(body);

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + apiKey)
                .addHeader("Content-Type", "application/json")
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

            if (saveHistory) addToHistory(reply);
            return reply;
        }
    }

    public Mono<PostSearchRequest> extractSearchRequest(String userQuery, boolean useMemory) {
        String prompt = """
        Hãy trích xuất thông tin từ câu hỏi sau và trả về đúng định dạng JSON với các trường:
        city, district, postType (HOME, APARTMENT, BUSINESS_PREMISES, ACCOMMODATION),
        bedRoom, bathRoom, minPrice, maxPrice, minArea, maxArea, title, description, amenities.
        
        `amenities` là mảng tiện ích (ví dụ: ["Wifi", "Máy lạnh"]).
        
        Ví dụ:
        {
          "city": "Hồ Chí Minh",
          "district": "Quận 1",
          "postType": "APARTMENT",
          "bedRoom": 2,
          "bathRoom": null,
          "minPrice": null,
          "maxPrice": 10000000,
          "minArea": null,
          "maxArea": null,
          "title": null,
          "description": null,
          "amenities": ["Wifi", "Máy lạnh"]
        }

        ❗️Yêu cầu bắt buộc:
        - Trả về **duy nhất** đoạn JSON như trên.
        - ❌ Không được thêm bất kỳ mô tả, markdown, ```json hoặc văn bản nào khác.

        Câu hỏi: """ + userQuery;

        Mono<String> responseMono = useMemory ? chat(prompt) : chatSingle(prompt);

        return responseMono.map(raw -> {
            String cleaned = cleanJson(raw);
            System.out.println("🧠 Cleaned Gemini JSON:\n" + cleaned);

            // Kiểm tra xem thực sự có phải JSON không
            if (!isLikelyJson(cleaned)) {
                throw new RuntimeException("❌ Gemini không trả về JSON hợp lệ:\n" + raw);
            }

            try {
                JsonElement element = JsonParser.parseString(cleaned);
                if (!element.isJsonObject()) {
                    throw new IllegalStateException("Expected JSON object but got: " + cleaned);
                }
                return gson.fromJson(element, PostSearchRequest.class);
            } catch (JsonSyntaxException e) {
                System.err.println("❌ Lỗi parse JSON từ Gemini:\n" + raw);
                throw new RuntimeException("Gemini trả về kết quả không hợp lệ: " + raw, e);
            }
        });
    }

    private boolean isLikelyJson(String text) {
        String trimmed = text.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }


    private String cleanJson(String raw) {
        String cleaned = raw.trim();

        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(json)?", "").trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }

        return cleaned;
    }


    public Mono<String> summarizeSearchResult(String userQuery, List<PostDocument> results, boolean useMemory) {
        if (results == null || results.isEmpty()) {
            // Trả về trực tiếp luôn 1 Mono, KHÔNG cần gọi Gemini
            return Mono.just("Hiện tại không có bài đăng nào phù hợp với nhu cầu tìm kiếm của bạn.");
        }

        return Mono.fromCallable(() -> {
            PostDocument post = results.get(0); // bài đăng phù hợp nhất

            String prompt = String.format("""
        Dựa vào bài đăng sau, hãy viết một đoạn văn mô tả ngắn gọn, rõ ràng, thân thiện, phù hợp với người đang tìm nhà cho thuê.
        Hãy tưởng tượng bạn là một chuyên viên tư vấn nhà đất đang trả lời cho khách hàng. KHÔNG TRẢ VỀ DẠNG JSON. Chỉ viết một đoạn văn mô tả...

        ❗️Yêu cầu bắt buộc:
        - Viết **một đoạn văn duy nhất**.
        - ❌ Không xuống dòng, không markdown, không JSON, không bắt đầu bằng ``` hoặc `{}`.
        - Câu văn phải bắt đầu bằng: **"Chúng tôi đã tìm thấy một bài đăng phù hợp với nhu cầu tìm kiếm của bạn..."**

        Thông tin bài đăng:
        - Tiêu đề: %s
        - Mô tả: %s
        - Địa chỉ: %s, %s, %s, %s
        - Giá: %,.0f VND
        - Diện tích: %.1f m²
        - Phòng ngủ: %d
        - Phòng vệ sinh: %d
        - Tiện ích: %s
        """,
                    post.getTitle(),
                    post.getDescription(),
                    post.getAddress(), post.getWard(), post.getDistrict(), post.getCity(),
                    post.getPrice() != null ? post.getPrice() : 0,
                    post.getArea() != null ? post.getArea() : 0,
                    post.getBedRoom() != null ? post.getBedRoom() : 0,
                    post.getBathRoom() != null ? post.getBathRoom() : 0,
                    post.getAmenities() != null ? String.join(", ", post.getAmenities()) : "Không rõ"
            );

            return prompt;
        }).flatMap(prompt -> useMemory ? chat(prompt) : chatSingle(prompt));
    }


}
