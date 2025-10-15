package org.example.my.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarAction {
    public enum ActionType {
        MOVE_FORWARD,
        MOVE_BACKWARD,
        TURN_LEFT,
        TURN_RIGHT,
        SHOOT,
        IDLE
    }

    private ActionType type;

    @Builder.Default
    private double power = 1.0; // от 0.0 до 1.0

    public CarAction(ActionType type) {
        this.type = type;
        this.power = 1.0;
    }

    public boolean requiresPower() {
        return type == ActionType.MOVE_FORWARD ||
                type == ActionType.MOVE_BACKWARD ||
                type == ActionType.TURN_LEFT ||
                type == ActionType.TURN_RIGHT;
    }
}