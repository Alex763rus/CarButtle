package org.example.my.controller;

import org.example.my.model.GameState;
import org.example.my.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class GameController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * WebSocket endpoint для старта игры
     */
    @MessageMapping("/game.start")
    public void startGameViaWebSocket(@Payload StartGameRequest request) {
        try {
            // Проверяем наличие обязательных полей
            if (request.getAiClass1() == null || request.getAiClass2() == null) {
                throw new IllegalArgumentException("Both AI classes must be specified");
            }

            gameService.startGame(request.getAiClass1(), request.getAiClass2());
            // Отправляем обновление состояния всем подписчикам
            messagingTemplate.convertAndSend("/topic/game.state", gameService.getGameState());

        } catch (Exception e) {
            System.err.println("Error starting game: " + e.getMessage());
            // Отправляем сообщение об ошибке
            GameState errorState = gameService.getGameState();
            errorState.setStatusMessage("Error starting game: " + e.getMessage());
            messagingTemplate.convertAndSend("/topic/game.state", errorState);
        }
    }

    /**
     * WebSocket endpoint для получения состояния игры
     */
    @MessageMapping("/game.state")
    public void getGameStateViaWebSocket() {
        messagingTemplate.convertAndSend("/topic/game.state", gameService.getGameState());
    }

    /**
     * WebSocket endpoint для остановки игры
     */
    @MessageMapping("/game.stop")
    public void stopGameViaWebSocket() {
        gameService.stopGame();
        messagingTemplate.convertAndSend("/topic/game.state", gameService.getGameState());
    }

    /**
     * WebSocket endpoint для сброса игры
     */
    @MessageMapping("/game.reset")
    public void resetGameViaWebSocket() {
        gameService.resetGame();
        messagingTemplate.convertAndSend("/topic/game.state", gameService.getGameState());
    }

    /**
     * WebSocket endpoint для отправки действия (без Principal)
     */
    @MessageMapping("/game.action")
    public void handlePlayerAction(@Payload PlayerAction action) {
        // Без аутентификации используем идентификатор из действия
        if (action.getCarId() != null) {
            gameService.handlePlayerAction(action.getCarId(), action);
        }
        // Отправляем обновление обратно отправителю
        messagingTemplate.convertAndSendToUser(action.getSessionId(), "/queue/game.state", gameService.getGameState());
    }

    /**
     * REST endpoint для старта игры
     */
    @PostMapping("/api/game/start")
    @ResponseBody
    public GameState startGame(@RequestBody StartGameRequest request) {
        gameService.startGame(request.getAiClass1(), request.getAiClass2());
        GameState state = gameService.getGameState();

        // Отправляем обновление через WebSocket
        messagingTemplate.convertAndSend("/topic/game.state", state);
        return state;
    }

    /**
     * REST endpoint для получения состояния игры
     */
    @GetMapping("/api/game/state")
    @ResponseBody
    public GameState getGameState() {
        return gameService.getGameState();
    }

    /**
     * REST endpoint для остановки игры
     */
    @PostMapping("/api/game/stop")
    @ResponseBody
    public GameState stopGame() {
        gameService.stopGame();
        GameState state = gameService.getGameState();
        messagingTemplate.convertAndSend("/topic/game.state", state);
        return state;
    }

    /**
     * REST endpoint для сброса игры
     */
    @PostMapping("/api/game/reset")
    @ResponseBody
    public GameState resetGame() {
        gameService.resetGame();
        GameState state = gameService.getGameState();
        messagingTemplate.convertAndSend("/topic/game.state", state);
        return state;
    }

    /**
     * REST endpoint для получения списка доступных ИИ
     */
    @GetMapping("/api/ai/available")
    @ResponseBody
    public List<AIClassInfo> getAvailableAI() {
        return List.of(
                new AIClassInfo("org.example.my.service.SimpleCarAI", "Simple AI", "Базовая реализация ИИ")
//                new AIClassInfo("org.example.my.service.AdvancedCarAI", "Advanced AI", "Продвинутая реализация ИИ"),
//                new AIClassInfo("org.example.my.service.RandomCarAI", "Random AI", "Случайные действия"),
//                new AIClassInfo("org.example.my.service.AggressiveCarAI", "Aggressive AI", "Агрессивная тактика")
        );
    }

    /**
     * Главная страница игры
     */
    @GetMapping("/")
    public String gamePage() {
        return "game";
    }

    /**
     * Страница управления
     */
    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    /**
     * Периодическая отправка состояния игры
     */
    @Scheduled(fixedRate = 50) // 20 FPS
    public void broadcastGameState() {
        if (gameService.isGameRunning()) {
            GameState state = gameService.getGameState();
            messagingTemplate.convertAndSend("/topic/game.state", state);
        }
    }

    /**
     * DTO для запроса старта игры
     */
    public static class StartGameRequest {
        private String aiClass1;
        private String aiClass2;
        private Map<String, Object> config;

        public StartGameRequest() {}

        public StartGameRequest(String aiClass1, String aiClass2) {
            this.aiClass1 = aiClass1;
            this.aiClass2 = aiClass2;
        }

        // Getters and Setters
        public String getAiClass1() { return aiClass1; }
        public void setAiClass1(String aiClass1) { this.aiClass1 = aiClass1; }

        public String getAiClass2() { return aiClass2; }
        public void setAiClass2(String aiClass2) { this.aiClass2 = aiClass2; }

        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    /**
     * DTO для действий игрока
     */
    public static class PlayerAction {
        private String actionType;
        private String carId;
        private String sessionId; // вместо Principal
        private Map<String, Object> parameters;

        // Getters and Setters
        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }

        public String getCarId() { return carId; }
        public void setCarId(String carId) { this.carId = carId; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public Map<String, Object> getParameters() { return parameters; }
        public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    }

    /**
     * DTO для информации об ИИ классе
     */
    public static class AIClassInfo {
        private String className;
        private String displayName;
        private String description;

        public AIClassInfo(String className, String displayName, String description) {
            this.className = className;
            this.displayName = displayName;
            this.description = description;
        }

        // Getters and Setters
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}