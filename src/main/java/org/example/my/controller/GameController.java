package org.example.my.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameEngine gameEngine;

    @PostMapping("/start")
    public Map<String, Object> startGame(@RequestBody(required = false) Map<String, Object> requestData) {
        gameEngine.initializeGame();

        // Если переданы данные о выборе AI, применяем их
        if (requestData != null) {
            String player1AI = (String) requestData.get("player1AI");
            String player2AI = (String) requestData.get("player2AI");
            String customAI = (String) requestData.get("customAI");

            if (player1AI != null && !player1AI.isEmpty()) {
                gameEngine.setPlayerAI(1, player1AI);
            }
            if (player2AI != null && !player2AI.isEmpty()) {
                gameEngine.setPlayerAI(2, player2AI);
            }
            // Для обратной совместимости
            if (customAI != null && !customAI.isEmpty()) {
                gameEngine.setPlayerAI(1, customAI);
            }
        }

        Map<String, Object> state = gameEngine.getGameState();
        state.put("status", "started");
        return state;
    }

    @PostMapping("/update")
    public Map<String, Object> updateGame() {
        gameEngine.updateGame();
        Map<String, Object> state = gameEngine.getGameState();
        state.put("status", "updated");
        return state;
    }

    @GetMapping("/state")
    public Map<String, Object> getGameState() {
        Map<String, Object> state = gameEngine.getGameState();
        state.put("status", gameEngine.isGameRunning() ? "running" : "stopped");
        return state;
    }

    @PostMapping("/stop")
    public Map<String, Object> stopGame() {
        gameEngine.stopGame();
        return Map.of("status", "stopped");
    }
}