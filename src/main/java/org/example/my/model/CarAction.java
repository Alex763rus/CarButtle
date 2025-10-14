package org.example.my.model;

import lombok.Data;

@Data
public class CarAction {
    public enum ActionType {
        MOVE_FORWARD, MOVE_BACKWARD, TURN_LEFT, TURN_RIGHT, SHOOT, IDLE
    }

    private ActionType action;
    private double intensity; // интенсивность действия (0-1)

    public CarAction(ActionType action, double intensity) {
        this.action = action;
        this.intensity = Math.max(0, Math.min(1, intensity));
    }
}