package org.example.my.ai.dynamic;

import org.example.my.ai.CarAI;
import org.example.my.ai.DynamicCarAI;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomAIManager {

    private final Map<String, DynamicCarAI> customAIs = new HashMap<>();

    public boolean registerCustomAI(String aiName, String javaCode) {
        try {
            DynamicCarAI dynamicAI = new DynamicCarAIImpl();
            dynamicAI.setCode(javaCode);

            if (dynamicAI.compileAndLoad()) {
                customAIs.put(aiName, dynamicAI);
                return true;
            } else {
                System.err.println("Compilation failed: " + dynamicAI.getCompilationError());
                // Все равно сохраняем, но помечаем как нерабочий
                customAIs.put(aiName, dynamicAI);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to register AI: " + e.getMessage());
            return false;
        }
    }

    public CarAI getCustomAI(String aiName) {
        DynamicCarAI dynamicAI = customAIs.get(aiName);
        return dynamicAI != null && dynamicAI.isLoaded() ? dynamicAI : null;
    }

    public Map<String, String> getAllCustomAINames() {
        Map<String, String> result = new HashMap<>();
        customAIs.forEach((name, ai) -> {
            try {
                // Используем безопасный метод
                result.put(name, ai.getCustomAIName());
            } catch (Exception e) {
                result.put(name, "Error: " + e.getMessage());
            }
        });
        return result;
    }

    public Map<String, String> getAllCustomAIStatuses() {
        Map<String, String> result = new HashMap<>();
        customAIs.forEach((name, ai) -> {
            String status = ai.isLoaded() ? "✓ Loaded" : "✗ Failed";
            String error = ai.getCompilationError();
            if (error != null && !error.isEmpty()) {
                status += " (" + error + ")";
            }
            result.put(name, status);
        });
        return result;
    }

    public boolean removeCustomAI(String aiName) {
        return customAIs.remove(aiName) != null;
    }

    public String getAIStatus(String aiName) {
        DynamicCarAI ai = customAIs.get(aiName);
        if (ai == null) return "Not found";
        return ai.isLoaded() ? "Loaded" : "Failed: " + ai.getCompilationError();
    }
}