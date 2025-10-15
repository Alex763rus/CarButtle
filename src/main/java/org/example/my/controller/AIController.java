package org.example.my.controller;

import org.example.my.ai.dynamic.CustomAIManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private CustomAIManager aiManager;

    @GetMapping("/editor")
    public String showAIEditor(Model model) {
        model.addAttribute("customAIs", aiManager.getAllCustomAINames());
        model.addAttribute("aiStatuses", aiManager.getAllCustomAIStatuses());
        return "ai-editor";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAI(
            @RequestParam String aiName,
            @RequestParam String javaCode) {

        boolean success = aiManager.registerCustomAI(aiName, javaCode);

        Map<String, Object> response = Map.of(
                "status", success ? "success" : "error",
                "message", success ? "AI successfully compiled and loaded" : "Compilation failed",
                "aiName", aiName,
                "customAIs", aiManager.getAllCustomAINames(), // Добавляем обновленный список
                "aiStatuses", aiManager.getAllCustomAIStatuses() // Добавляем статусы
        );

        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/template")
    @ResponseBody
    public String getAITemplate() {
        return """
            import org.example.my.model.Bullet;
            import org.example.my.model.Car;
            import org.example.my.model.CarAction;
            import org.example.my.model.Position;
            
            import java.util.Collection;
            
            public class UserCarAI implements org.example.my.ai.CarAI {
                
                @Override
                public CarAction decideAction(Car myCar, Car opponentCar, Collection<Bullet> bullets) {
                    // ВАШ КОД ЗДЕСЬ
                    // Всегда проверяем на null и валидность
                    if (myCar == null || !myCar.isAlive() || opponentCar == null || !opponentCar.isAlive()) {
                        return new CarAction(CarAction.ActionType.IDLE);
                    }
                    
                    try {
                        // Пример: всегда двигаться вперед и иногда стрелять
                        double distance = Math.sqrt(
                            Math.pow(myCar.getPosition().getX() - opponentCar.getPosition().getX(), 2) +
                            Math.pow(myCar.getPosition().getY() - opponentCar.getPosition().getY(), 2)
                        );
                        
                        if (distance < 200 && myCar.canShoot()) {
                            return new CarAction(CarAction.ActionType.SHOOT);
                        }
                        
                        return new CarAction(CarAction.ActionType.MOVE_FORWARD, 0.7);
                    } catch (Exception e) {
                        // В случае ошибки возвращаем безопасное действие
                        return new CarAction(CarAction.ActionType.IDLE);
                    }
                }
                
                @Override
                public String getAIName() {
                    return "My Custom AI";
                }
            }
            """;
    }

    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getAIStatus() {
        return Map.of(
                "customAIs", aiManager.getAllCustomAINames(),
                "statuses", aiManager.getAllCustomAIStatuses()
        );
    }

    @DeleteMapping("/{aiName}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAI(@PathVariable String aiName) {
        boolean removed = aiManager.removeCustomAI(aiName);
        Map<String, Object> response = Map.of(
                "status", removed ? "success" : "error",
                "message", removed ? "AI deleted successfully" : "AI not found",
                "customAIs", aiManager.getAllCustomAINames(),
                "aiStatuses", aiManager.getAllCustomAIStatuses()
        );
        return removed ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // Новый endpoint для получения списка AI
    @GetMapping("/list")
    @ResponseBody
    public Map<String, Object> getAIList() {
        return Map.of(
                "customAIs", aiManager.getAllCustomAINames(),
                "aiStatuses", aiManager.getAllCustomAIStatuses()
        );
    }
}