package org.example.my.model;

import lombok.Data;

@Data
public class Car {
    private String id;
    private Position position;
    private int health;
    private int ammo;
    private boolean isAlive;
    private long lastShotTime;
    private String aiClass; // класс ИИ для управления

    public Car(String id, double startX, double startY, String aiClass) {
        this.id = id;
        this.position = new Position(startX, startY, 0);
        this.health = 100;
        this.ammo = 50;
        this.isAlive = true;
        this.aiClass = aiClass;
    }

    public boolean canShoot() {
        return ammo > 0 && System.currentTimeMillis() - lastShotTime > 1000;
    }
}
