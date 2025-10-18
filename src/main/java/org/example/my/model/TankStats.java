package org.example.my.model;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class TankStats {
    @Builder.Default
    private int shootingRange = 1;    // Дальность выстрела (1-5)
    @Builder.Default
    private int movementSpeed = 1;    // Скорость перемещения (1-5)
    @Builder.Default
    private int fireRate = 1;         // Скорострельность (1-5)

    private static final int MAX_POINTS = 10;
    private static final int MAX_STAT_VALUE = 5;

    public boolean isValid() {
        int total = shootingRange + movementSpeed + fireRate;
        return total <= MAX_POINTS &&
                shootingRange >= 1 && shootingRange <= MAX_STAT_VALUE &&
                movementSpeed >= 1 && movementSpeed <= MAX_STAT_VALUE &&
                fireRate >= 1 && fireRate <= MAX_STAT_VALUE;
    }

    public int getRemainingPoints() {
        return MAX_POINTS - (shootingRange + movementSpeed + fireRate);
    }

    public String toDisplayString() {
        return String.format("Дальность: %d/5, Скорость: %d/5, Скорострельность: %d/5 (Осталось очков: %d)",
                shootingRange, movementSpeed, fireRate, getRemainingPoints());
    }
}