package org.example.my.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class Position {
    @Builder.Default
    private double x = 0;

    @Builder.Default
    private double y = 0;

    @Builder.Default
    private double angle = 0;

    public Position(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public double distanceTo(Position other) {
        if (other == null) return Double.MAX_VALUE;
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Position copy() {
        return new Position(x, y, angle);
    }

    // РУЧНЫЕ ГЕТТЕРЫ
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }
}