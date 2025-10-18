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
    public Map<String, Object> startGame(@RequestBody(required = false) Map<String, String> requestData) {
        gameEngine.initializeGame();

        // Если переданы данные о выборе AI, применяем их
        if (requestData != null) {
            String player1AI = requestData.get("player1AI");
            String player2AI = requestData.get("player2AI");

            if (player1AI != null && !player1AI.isEmpty()) {
                gameEngine.setPlayerAI(1, player1AI);
            }
            if (player2AI != null && !player2AI.isEmpty()) {
                gameEngine.setPlayerAI(2, player2AI);
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