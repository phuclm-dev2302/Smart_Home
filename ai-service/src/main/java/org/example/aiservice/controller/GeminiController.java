package org.example.aiservice.controller;

import org.example.aiservice.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/ai")
public class GeminiController {
    @Autowired
    private  GeminiService geminiService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String text) {
        try {
            String reply = geminiService.chat(text);
            return ResponseEntity.ok(reply);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gemini API error: " + e.getMessage());
        }
    }
}
