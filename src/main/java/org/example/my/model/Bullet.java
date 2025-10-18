package org.example.my.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bullet {
    @Builder.Default
    private double x = 0.0;

    @Builder.Default
    private double y = 0.0;

    @Builder.Default
    private double angle = 0.0;

    @Builder.Default
    private double speed = 4.0;

    @Builder.Default
    private double damage = 25.0;

    @Builder.Default
    private int lifetime = 2000;

    private Car owner;

    @Builder.Default
    private long creationTime = System.currentTimeMillis();

    @Builder.Default
    private boolean active = true;

    public void update() {
        if (!active) return;

        double radianAngle = Math.toRadians(angle);
        x += Math.cos(radianAngle) * speed;
        y += Math.sin(radianAngle) * speed;

        // Проверка времени жизни
        if (System.currentTimeMillis() - creationTime > lifetime) {
            active = false;
        }

        // Проверка границ
        if (x < 0 || x > 800 || y < 0 || y > 600) {
            active = false;
        }
    }

    public Position getPosition() {
        return new Position(x, y, angle);
    }

    public void deactivate() {
        this.active = false;
    }

    // РУЧНО ДОБАВИМ ГЕТТЕР
    public boolean isActive() {
        return active;
    }

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