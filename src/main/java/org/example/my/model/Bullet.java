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
    private double x = 0;
    @Builder.Default
    private double y = 0;
    @Builder.Default
    private double angle = 0;
    @Builder.Default
    private double speed = 10.0;
    @Builder.Default
    private double damage = 25;
    @Builder.Default
    private int lifetime = 2000; // 2 seconds
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

    // Добавляем метод deactivate()
    public void deactivate() {
        this.active = false;
    }

    // Альтернативно можно использовать setActive(false)
    public void setActive(boolean active) {
        this.active = active;
    }
}