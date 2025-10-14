package org.example.my.model;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameState {
    private Map<String, Car> cars = new ConcurrentHashMap<>();
    private Map<String, Bullet> bullets = new ConcurrentHashMap<>();
    private boolean gameRunning = false;
    private long gameStartTime;
}