package org.example.my.controller;

import org.example.my.model.GameState;
import org.example.my.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

// GameController.java
@Controller
public class GameController {

    @Autowired
    private GameService gameService;

    @MessageMapping("/game.start")
    @SendTo("/topic/game.state")
    public GameState startGame(@Payload StartGameRequest request) {
        gameService.startGame(request.getAiClass1(), request.getAiClass2());
        return gameService.getGameState();
    }

    @MessageMapping("/game.state")
    @SendTo("/topic/game.state")
    public GameState getGameState() {
        return gameService.getGameState();
    }
}