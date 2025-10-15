package org.example.my.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Car {
    @Builder.Default
    private Position position = new Position(0, 0, 0);

    @Builder.Default
    private double speed = 0;

    @Builder.Default
    private double maxSpeed = 5.0;

    @Builder.Default
    private double acceleration = 0.2;

    @Builder.Default
    private double rotationSpeed = 4.0;

    @Builder.Default
    private int health = 100;

    @Builder.Default
    private int maxHealth = 100;

    @Builder.Default
    private boolean alive = true;

    @Builder.Default
    private long lastShotTime = 0;

    @Builder.Default
    private long shootCooldown = 1000; // 1 second cooldown

    @Builder.Default
    private String name = "Car";

    @Builder.Default
    private int ammo = 30; // Добавляем поле ammo

    @Builder.Default
    private int maxAmmo = 30;

    @Builder.Default
    private int score = 0;

    // Конструктор для удобного создания
    public Car(double x, double y, double angle) {
        this.position = new Position(x, y, angle);
    }

    public void update() {
        // Обновление позиции на основе скорости и направления
        double radianAngle = Math.toRadians(position.getAngle());
        double newX = position.getX() + Math.cos(radianAngle) * speed;
        double newY = position.getY() + Math.sin(radianAngle) * speed;

        // Проверка границ
        newX = Math.max(0, Math.min(800, newX));
        newY = Math.max(0, Math.min(600, newY));

        position = new Position(newX, newY, position.getAngle());

        // Постепенное замедление
        if (speed > 0) {
            speed = Math.max(0, speed - 0.05);
        } else if (speed < 0) {
            speed = Math.min(0, speed + 0.05);
        }
    }

    public void moveForward(double power) {
        power = Math.max(0, Math.min(1, power));
        speed = Math.min(maxSpeed, speed + acceleration * power);
    }

    public void moveBackward(double power) {
        power = Math.max(0, Math.min(1, power));
        speed = Math.max(-maxSpeed * 0.5, speed - acceleration * power);
    }

    public void turnLeft(double power) {
        power = Math.max(0, Math.min(1, power));
        double newAngle = position.getAngle() - rotationSpeed * power;
        position.setAngle(normalizeAngle(newAngle));
    }

    public void turnRight(double power) {
        power = Math.max(0, Math.min(1, power));
        double newAngle = position.getAngle() + rotationSpeed * power;
        position.setAngle(normalizeAngle(newAngle));
    }

    public Bullet shoot() {
        long currentTime = System.currentTimeMillis();
        if (canShoot()) {
            lastShotTime = currentTime;

            // Создаем пулю в направлении машины
            double bulletSpeed = 10.0;
            double radianAngle = Math.toRadians(position.getAngle());
            double startX = position.getX() + Math.cos(radianAngle) * 25; // Пуля появляется перед танком
            double startY = position.getY() + Math.sin(radianAngle) * 25;

            return Bullet.builder()
                    .x(startX)
                    .y(startY)
                    .angle(position.getAngle())
                    .speed(bulletSpeed)
                    .owner(this)
                    .damage(25)
                    .lifetime(2000) // 2 секунды жизни
                    .active(true)
                    .build();
        }
        return null;
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        return alive && (currentTime - lastShotTime) >= shootCooldown;
    }

    public void addAmmo(int amount) {
        this.ammo = Math.min(maxAmmo, ammo + amount);
    }

    public void reload() {
        this.ammo = maxAmmo;
    }

    public void takeDamage(int damage) {
        if (alive) {
            health = Math.max(0, health - damage);
            if (health <= 0) {
                alive = false;
                speed = 0;
            }
        }
    }

    public void heal(int amount) {
        if (alive) {
            health = Math.min(maxHealth, health + amount);
        }
    }

    public void respawn(double x, double y, double angle) {
        this.position = new Position(x, y, angle);
        this.health = maxHealth;
        this.speed = 0;
        this.alive = true;
        this.lastShotTime = System.currentTimeMillis();
    }

    public void addScore(int points) {
        this.score += points;
    }

    private double normalizeAngle(double angle) {
        while (angle > 360) angle -= 360;
        while (angle < 0) angle += 360;
        return angle;
    }
}